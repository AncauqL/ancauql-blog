<template>
  <section class="home-page">
    <div class="home-content">
      <header class="home-header">
        <h1>文章列表</h1>
      </header>

      <div class="article-feed">
        <article
            v-for="item in articleList"
            :key="item.id"
            class="article-card"
            @click="goDetail(item.id)"
        >
          <div class="article-main">
            <h2>{{ item.title }}</h2>
            <p class="summary">{{ item.summary || '暂无摘要' }}</p>
            <div class="meta">
              <span>{{ formatTime(item.createTime) }}</span>
              <span>阅读 {{ item.viewCount || 0 }}</span>
            </div>
          </div>
        </article>

        <el-empty
            v-if="loaded && articleList.length === 0"
            description="暂无文章"
        />
      </div>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
            background
            layout="prev, pager, next"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            @current-change="onPageChange"
        />
      </div>
    </div>
  </section>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'HomeView',
  data() {
    return {
      articleList: [],
      loaded: false,
      pageNum: 1,
      pageSize: 6,
      total: 0
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      // 服务端分页；status=published 保证管理员登录后首页看到的也是公开视角
      request.get('/article/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: 'published'
        }
      }).then(res => {
        this.loaded = true
        if (res.code === '200') {
          this.articleList = (res.data && res.data.records) || []
          this.total = (res.data && res.data.total) || 0
        } else {
          this.$message.error(res.msg)
        }
      }).catch(() => {
        this.loaded = true
        this.$message.error('文章列表加载失败，请确认后端已启动')
      })
    },
    onPageChange(page) {
      this.pageNum = page
      this.load()
      window.scrollTo({ top: 0 })
    },
    goDetail(id) {
      this.$router.push('/post/' + id)
    },
    formatTime(value) {
      if (!value) {
        return '未知日期'
      }

      if (Array.isArray(value)) {
        const year = value[0]
        const month = this.padZero(value[1])
        const day = this.padZero(value[2])
        return `${year}-${month}-${day}`
      }

      return String(value).replace('T', ' ').slice(0, 10)
    },
    padZero(value) {
      return String(value).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 120px);
  padding: 28px 20px 56px;
  color: #1f1e33;
}

.home-content {
  max-width: 860px;
  margin: 0 auto;
}

.home-header {
  margin-bottom: 26px;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", "Times New Roman", serif;
}

.home-header h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.4;
}

.home-header p {
  margin: 8px 0 0;
  font-size: 17px;
  line-height: 1.8;
  color: #606266;
}

.article-feed {
  border-top: 1px solid #ebeef5;
}

.article-card {
  padding: 22px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.article-card:hover {
  color: #409eff;
  transform: translateX(4px);
}

.article-main h2 {
  margin: 0;
  font-size: 23px;
  line-height: 1.45;
  font-weight: 600;
}

.summary {
  margin: 10px 0 0;
  font-size: 15px;
  line-height: 1.9;
  color: #606266;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
