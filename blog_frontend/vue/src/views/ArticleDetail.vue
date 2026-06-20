<template>
  <section class="detail-page">
    <article class="detail-content" v-if="article">
      <el-button
          type="text"
          class="back-button"
          @click="$router.push('/')"
      >
        返回首页
      </el-button>

      <h1>{{ article.title }}</h1>

      <div class="meta">
        <span>{{ formatTime(article.createTime) }}</span>
        <span>{{ getStatusText(article.status) }}</span>
        <span>阅读 {{ article.viewCount || 0 }}</span>
      </div>

      <p v-if="article.summary" class="summary">
        {{ article.summary }}
      </p>

      <div class="content">
        {{ article.content }}
      </div>
    </article>

    <el-empty
        v-else
        description="文章不存在"
    />
  </section>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'ArticleDetail',
  data() {
    return {
      article: null
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get('/article/detail', {
        params: { id: this.$route.params.id }
      }).then(res => {
        if (res.code === '200') {
          this.article = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
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
        const hour = this.padZero(value[3] || 0)
        const minute = this.padZero(value[4] || 0)
        return `${year}-${month}-${day} ${hour}:${minute}`
      }

      return String(value).replace('T', ' ').slice(0, 16)
    },
    padZero(value) {
      return String(value).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 120px);
  padding: 28px 20px 64px;
  color: #1f1e33;
}

.detail-content {
  max-width: 820px;
  margin: 0 auto;
}

.back-button {
  padding: 0;
  margin-bottom: 18px;
}

.detail-content h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.45;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", "Times New Roman", serif;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 12px;
  color: #909399;
  font-size: 14px;
}

.summary {
  margin: 28px 0 0;
  padding-left: 16px;
  border-left: 3px solid #1f1e33;
  font-size: 17px;
  line-height: 1.9;
  color: #606266;
}

.content {
  margin-top: 30px;
  font-size: 17px;
  line-height: 2;
  color: #303133;
  white-space: pre-wrap;
}
</style>
