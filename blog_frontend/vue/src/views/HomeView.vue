<template>
  <section class="home-page">
    <div class="home-content">
      <!-- 站点门面 -->
      <header class="hero">
        <h1>{{ site.name }}</h1>
        <p class="hero-slogan">{{ site.slogan }}</p>
      </header>

      <!-- 分类筛选 -->
      <div v-if="categoryList.length" class="category-bar">
        <span
            :class="['category-chip', { active: activeCategoryId === null }]"
            @click="selectCategory(null)"
        >
          全部
        </span>
        <span
            v-for="item in categoryList"
            :key="item.id"
            :class="['category-chip', { active: activeCategoryId === item.id }]"
            @click="selectCategory(item.id)"
        >
          {{ item.name }}
        </span>
      </div>

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
              <span v-if="categoryName(item.categoryId)">
                {{ categoryName(item.categoryId) }}
              </span>
              <span>阅读 {{ item.viewCount || 0 }}</span>
            </div>
          </div>
          <img
              v-if="item.cover"
              :src="resolveAsset(item.cover)"
              class="article-cover"
              alt=""
          >
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
import request, { resolveAsset } from '@/utils/request'
import { SITE } from '@/config/site'

export default {
  name: 'HomeView',
  data() {
    return {
      site: SITE,
      articleList: [],
      categoryList: [],
      activeCategoryId: null,
      loaded: false,
      pageNum: 1,
      pageSize: 6,
      total: 0
    }
  },
  created() {
    this.loadCategories()
    this.load()
  },
  methods: {
    resolveAsset,
    load() {
      // 服务端分页；status=published 保证管理员登录后首页看到的也是公开视角
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        status: 'published'
      }
      if (this.activeCategoryId !== null) {
        params.categoryId = this.activeCategoryId
      }
      request.get('/article/selectPage', { params }).then(res => {
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
    loadCategories() {
      request.get('/category/selectAll').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data || []
        }
      }).catch(() => {})
    },
    selectCategory(id) {
      this.activeCategoryId = id
      this.pageNum = 1
      this.load()
    },
    categoryName(categoryId) {
      const category = this.categoryList.find(item => item.id === categoryId)
      return category ? category.name : ''
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
  padding: 40px 20px 56px;
  color: #1f1e33;
}

.home-content {
  max-width: 860px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 30px;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", "Times New Roman", serif;
}

.hero h1 {
  margin: 0;
  font-size: 38px;
  line-height: 1.4;
}

.hero-slogan {
  margin: 10px 0 0;
  font-size: 16px;
  color: #909399;
  letter-spacing: 1px;
}

.category-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.category-chip {
  padding: 4px 14px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.category-chip:hover {
  color: #1f1e33;
  border-color: #c0c4cc;
}

.category-chip.active {
  background: #1f1e33;
  border-color: #1f1e33;
  color: #fff;
}

.article-feed {
  border-top: 1px solid #ebeef5;
  margin-top: 16px;
}

.article-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 22px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.article-card:hover {
  color: #409eff;
  transform: translateX(4px);
}

.article-main {
  flex: 1;
  min-width: 0;
}

.article-main h2 {
  margin: 0;
  font-size: 23px;
  line-height: 1.45;
  font-weight: 600;
}

.article-cover {
  width: 148px;
  height: 96px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.summary {
  margin: 10px 0 0;
  font-size: 15px;
  line-height: 1.9;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

@media (max-width: 640px) {
  .home-page {
    padding: 24px 16px 40px;
  }

  .hero h1 {
    font-size: 30px;
  }

  .article-cover {
    width: 96px;
    height: 68px;
  }

  .article-main h2 {
    font-size: 19px;
  }
}
</style>
