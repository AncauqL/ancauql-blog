<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div>
        <h2>数据统计</h2>
        <p>前台每次页面访问自动上报（管理员自己的浏览不计入）</p>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="days" size="small" @change="load">
          <el-radio-button :label="7">近 7 天</el-radio-button>
          <el-radio-button :label="30">近 30 天</el-radio-button>
          <el-radio-button :label="90">近 90 天</el-radio-button>
        </el-radio-group>
        <el-button size="small" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <el-alert
        v-if="!loading && overview.totalPv === 0"
        type="info"
        :closable="false"
        show-icon
        title="还没有统计数据"
        description="用浏览器打开一次前台页面（首页或任意文章），再回来点「刷新」就能看到数据了。"
        class="mb"
    />

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">今日浏览量 PV</div>
        <div class="stat-value">{{ overview.todayPv }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日访客 UV</div>
        <div class="stat-value">{{ overview.todayUv }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">近 {{ days }} 天 PV</div>
        <div class="stat-value">{{ overview.rangePv }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">近 {{ days }} 天 UV</div>
        <div class="stat-value">{{ overview.rangeUv }}</div>
        <div class="stat-tip">按天去重后求和</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">累计 PV</div>
        <div class="stat-value">{{ overview.totalPv }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">累计 UV</div>
        <div class="stat-value">{{ overview.totalUv }}</div>
        <div class="stat-tip">跨天去重</div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">
        <h3>访问趋势（近 {{ days }} 天）</h3>
        <span class="legend">
          <i class="dot pv"></i>PV
          <i class="dot uv"></i>UV
        </span>
      </div>
      <div v-if="daily.length" class="chart">
        <div
            v-for="item in daily"
            :key="item.statDate"
            class="chart-col"
            :title="`${item.statDate} · PV ${item.pv} · UV ${item.uv}`"
        >
          <div class="bars">
            <div class="bar pv" :style="{ height: heightOf(item.pv) }"></div>
            <div class="bar uv" :style="{ height: heightOf(item.uv) }"></div>
          </div>
          <div class="bar-label">{{ shortDate(item.statDate) }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无趋势数据" :image-size="60" />
    </div>

    <div class="panel">
      <div class="panel-head">
        <h3>热门文章（近 {{ days }} 天访问量 Top 10）</h3>
      </div>
      <el-table :data="topArticles" border stripe>
        <el-table-column label="#" width="60" align="center">
          <template slot-scope="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column label="标题" min-width="240">
          <template slot-scope="scope">
            <router-link
                v-if="scope.row.articleId"
                :to="'/post/' + scope.row.articleId"
                class="post-link"
            >{{ scope.row.title }}</router-link>
            <span v-else>{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pv" label="访问量" width="120" align="center" />
      </el-table>
      <el-empty
          v-if="topArticles.length === 0"
          description="这段时间还没有文章访问记录"
          :image-size="60"
      />
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Dashboard',
  data() {
    return {
      days: 30,
      loading: false,
      overview: {
        todayPv: 0,
        todayUv: 0,
        rangePv: 0,
        rangeUv: 0,
        totalPv: 0,
        totalUv: 0
      },
      daily: [],
      topArticles: []
    }
  },
  computed: {
    maxPv() {
      return this.daily.reduce((max, item) => Math.max(max, item.pv || 0), 0)
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      request.get('/visit/dashboard', { params: { days: this.days } })
          .then(res => {
            if (res.code === '200' && res.data) {
              this.overview = res.data.overview || this.overview
              this.daily = res.data.daily || []
              this.topArticles = res.data.topArticles || []
            } else {
              this.$message.error(res.msg || '统计读取失败')
            }
          })
          .catch(() => {
            this.$message.error('统计读取失败，请确认后端已启动')
          })
          .finally(() => {
            this.loading = false
          })
    },
    /** 柱高按当期最大值归一化，至少留一点可见高度 */
    heightOf(value) {
      const max = Math.max(this.maxPv, 1)
      const percent = Math.round(((value || 0) / max) * 100)
      return (value > 0 ? Math.max(percent, 3) : 0) + '%'
    },
    shortDate(value) {
      // 只显示 月-日，且过密时由 CSS 隐藏部分标签
      return String(value || '').slice(5)
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.mb {
  margin-bottom: 16px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px 18px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-tip {
  margin-top: 4px;
  font-size: 11px;
  color: #c0c4cc;
}

.panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px 18px;
  margin-bottom: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.legend {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.legend .dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  display: inline-block;
  margin-left: 8px;
}

.legend .dot.pv {
  background: #1f1e33;
}

.legend .dot.uv {
  background: #a8abb2;
}

.chart {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 200px;
  padding-top: 8px;
  overflow-x: auto;
}

.chart-col {
  flex: 1 0 10px;
  min-width: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.bars {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 1px;
  width: 100%;
  height: 100%;
}

.bar {
  width: 46%;
  max-width: 12px;
  border-radius: 2px 2px 0 0;
  transition: height 0.3s ease;
}

.bar.pv {
  background: #1f1e33;
}

.bar.uv {
  background: #c8c9cc;
}

.bar-label {
  margin-top: 6px;
  font-size: 10px;
  color: #c0c4cc;
  white-space: nowrap;
}

.post-link {
  color: #303133;
  text-decoration: none;
}

.post-link:hover {
  color: #409eff;
}
</style>
