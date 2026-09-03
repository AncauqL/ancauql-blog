<template>
  <section class="archive-page">
    <div class="archive-content">
      <header class="archive-header">
        <h1>归档</h1>
        <p v-if="totalCount">共 {{ totalCount }} 篇文章</p>
      </header>

      <div v-for="group in groups" :key="group.year" class="year-group">
        <h2 class="year-title">
          {{ group.year }}
          <span class="year-count">{{ group.articles.length }} 篇</span>
        </h2>
        <ul class="article-list">
          <router-link
              v-for="item in group.articles"
              :key="item.id"
              :to="'/post/' + item.id"
              class="archive-item"
          >
            <span class="item-date">{{ formatMonthDay(item.createTime) }}</span>
            <span class="item-title">{{ item.title }}</span>
          </router-link>
        </ul>
      </div>

      <el-empty
          v-if="loaded && groups.length === 0"
          description="暂无文章"
      />
    </div>
  </section>
</template>

<script>
import request from '@/utils/request'
import { formatMonthDay } from '@/utils/datetime'

export default {
  name: 'Archive',
  data() {
    return {
      groups: [],
      loaded: false
    }
  },
  computed: {
    totalCount() {
      return this.groups.reduce((sum, g) => sum + g.articles.length, 0)
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get('/article/archive').then(res => {
        this.loaded = true
        if (res.code === '200') {
          this.groups = res.data || []
        } else {
          this.$message.error(res.msg)
        }
      }).catch(() => {
        this.loaded = true
        this.$message.error('归档加载失败，请确认后端已启动')
      })
    },
    formatMonthDay(value) {
      const s = formatMonthDay(value)
      return s || '--'
    }
  }
}
</script>

<style scoped>
.archive-page {
  padding: 40px 20px 56px;
  color: #1f1e33;
}

.archive-content {
  max-width: 720px;
  margin: 0 auto;
}

.archive-header {
  margin-bottom: 28px;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", serif;
}

.archive-header h1 {
  margin: 0;
  font-size: 34px;
}

.archive-header p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.year-group {
  margin-bottom: 28px;
}

.year-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", serif;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.year-count {
  font-size: 13px;
  color: #c0c4cc;
  font-weight: normal;
  margin-left: 8px;
}

.article-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.archive-item {
  display: flex;
  align-items: baseline;
  gap: 16px;
  padding: 9px 4px;
  border-radius: 4px;
  cursor: pointer;
  color: inherit;
  text-decoration: none;
  transition: background 0.15s ease, transform 0.15s ease;
}

.archive-item:hover {
  background: #f8f9fb;
  transform: translateX(4px);
}

.item-date {
  font-size: 13px;
  color: #909399;
  font-family: Consolas, monospace;
  flex-shrink: 0;
}

.item-title {
  font-size: 16px;
  color: #303133;
}

.archive-item:hover .item-title {
  color: #409eff;
}
</style>
