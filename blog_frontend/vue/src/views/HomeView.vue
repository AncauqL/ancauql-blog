<template>
  <section class="home-page">
    <div class="home-content">
      <header class="home-header">
        <h1>文章列表</h1>
      </header>

      <div class="article-feed">
        <article
            v-for="item in pagedArticles"
            :key="item.id"
            class="article-card"
            @click="goDetail(item.id)"
        >
          <div class="article-main">
            <h2>{{ item.title }}</h2>
            <p class="summary">{{ item.summary || getContentPreview(item.content) }}</p>
            <div class="meta">
              <span>{{ formatTime(item.createTime) }}</span>
              <span>阅读 {{ item.viewCount || 0 }}</span>
            </div>
          </div>
        </article>

        <el-empty
            v-if="articleList.length === 0"
            description="暂无文章"
        />
      </div>

      <div v-if="articleList.length > pageSize" class="pagination-wrap">
        <el-pagination
            background
            layout="prev, pager, next"
            :current-page.sync="pageNum"
            :page-size="pageSize"
            :total="articleList.length"
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
      pageNum: 1,
      pageSize: 6
    }
  },
  computed: {
    sortedArticles() {
      return [...this.articleList].sort((a, b) => {
        return this.getTimeValue(b.createTime) - this.getTimeValue(a.createTime)
      })
    },
    pagedArticles() {
      const start = (this.pageNum - 1) * this.pageSize
      return this.sortedArticles.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get('/article/selectAll').then(res => {
        if (res.code === '200') {
          this.articleList = (res.data || []).filter(item => {
            return item.status !== 'draft'
          })
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    goDetail(id) {
      this.$router.push('/post/' + id)
    },
    getContentPreview(content) {
      if (!content) {
        return '暂无摘要'
      }
      return content.replace(/\s+/g, ' ').slice(0, 90)
    },
    getStatusText(status) {
      if (status === 'published') {
        return '已发布'
      }
      if (status === 'draft') {
        return '草稿'
      }
      return '未知状态'
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
    getTimeValue(value) {
      if (!value) {
        return 0
      }

      if (Array.isArray(value)) {
        return new Date(value[0], value[1] - 1, value[2] || 1).getTime()
      }

      return new Date(value).getTime() || 0
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
