<template>
  <section class="detail-page">
    <div v-if="article" class="detail-layout">
      <article class="detail-content">
        <el-button
            type="text"
            class="back-button"
            @click="$router.push('/')"
        >
          ← 返回首页
        </el-button>

        <h1>{{ article.title }}</h1>

        <div class="meta">
          <span>{{ formatTime(article.createTime) }}</span>
          <span v-if="categoryName">{{ categoryName }}</span>
          <span>阅读 {{ article.viewCount || 0 }}</span>
          <span v-if="wordCount">全文 {{ wordCount }} 字</span>
          <span v-if="minutes">约 {{ minutes }} 分钟读完</span>
          <el-tag
              v-if="article.status === 'draft'"
              size="mini"
              type="warning"
          >
            草稿预览
          </el-tag>
        </div>

        <img
            v-if="article.cover"
            :src="resolveAsset(article.cover)"
            loading="lazy"
            class="cover-hero"
            alt=""
        >

        <p v-if="article.summary" class="summary">
          {{ article.summary }}
        </p>

        <div
            ref="content"
            class="markdown-body"
            @click="onContentClick"
            v-html="renderedContent"
        ></div>

        <nav v-if="neighbors.prev || neighbors.next" class="post-nav">
          <router-link
              v-if="neighbors.prev"
              :to="'/post/' + neighbors.prev.id"
              class="post-nav-card"
          >
            <span class="post-nav-label">上一篇</span>
            <span class="post-nav-title">{{ neighbors.prev.title }}</span>
          </router-link>
          <span v-else class="post-nav-placeholder"></span>

          <router-link
              v-if="neighbors.next"
              :to="'/post/' + neighbors.next.id"
              class="post-nav-card post-nav-card--next"
          >
            <span class="post-nav-label">下一篇</span>
            <span class="post-nav-title">{{ neighbors.next.title }}</span>
          </router-link>
          <span v-else class="post-nav-placeholder"></span>
        </nav>
      </article>

      <aside v-if="tocItems.length >= 2" class="toc">
        <div class="toc-inner">
          <div class="toc-title">目录</div>
          <ul>
            <li
                v-for="item in tocItems"
                :key="item.id"
                :class="[
                  'toc-item',
                  'toc-level-' + item.indent,
                  { active: item.id === activeHeading }
                ]"
                @click="scrollToHeading(item.id)"
            >
              {{ item.text }}
            </li>
          </ul>
        </div>
      </aside>
    </div>

    <el-empty
        v-else-if="loaded"
        :description="errorText"
    />
  </section>
</template>

<script>
import request, { resolveAsset } from '@/utils/request'
import { renderMarkdown, countWords, readingMinutes } from '@/utils/markdown'
import { formatDateTime } from '@/utils/datetime'

export default {
  name: 'ArticleDetail',
  data() {
    return {
      article: null,
      categoryList: [],
      loaded: false,
      errorText: '文章不存在',
      neighbors: {
        prev: null,
        next: null
      },
      tocItems: [],
      activeHeading: '',
      observer: null
    }
  },
  computed: {
    renderedContent() {
      return this.article ? renderMarkdown(this.article.content) : ''
    },
    wordCount() {
      return this.article ? countWords(this.article.content) : 0
    },
    minutes() {
      return this.article ? readingMinutes(this.article.content) : 0
    },
    categoryName() {
      if (!this.article || !this.article.categoryId) {
        return ''
      }
      const category = this.categoryList.find(
          item => item.id === this.article.categoryId)
      return category ? category.name : ''
    }
  },
  watch: {
    // 上一篇 / 下一篇跳转复用同一组件，路由参数变化时重新加载
    '$route.params.id'() {
      this.reset()
      this.load()
    }
  },
  created() {
    this.load()
    this.loadCategories()
  },
  beforeDestroy() {
    this.disconnectObserver()
  },
  methods: {
    resolveAsset,
    reset() {
      this.disconnectObserver()
      this.article = null
      this.loaded = false
      this.errorText = '文章不存在'
      this.neighbors = { prev: null, next: null }
      this.tocItems = []
      this.activeHeading = ''
      window.scrollTo(0, 0)
    },
    load() {
      const id = this.$route.params.id
      request.get('/article/detail', {
        params: { id }
      }).then(res => {
        this.loaded = true
        if (res.code === '200') {
          this.article = res.data
          this.loadNeighbors(id)
          this.$nextTick(() => {
            this.buildToc()
          })
        } else {
          this.errorText = res.code === '403'
              ? '这篇文章尚未发布'
              : (res.msg || '文章不存在')
        }
      }).catch(() => {
        this.loaded = true
        this.errorText = '加载失败，请稍后重试'
      })
    },
    loadNeighbors(id) {
      request.get('/article/neighbors', {
        params: { id }
      }).then(res => {
        if (res.code === '200' && res.data) {
          this.neighbors = {
            prev: res.data.prev || null,
            next: res.data.next || null
          }
        }
      }).catch(() => {
        // 导航失败不影响正文阅读，静默处理
      })
    },
    loadCategories() {
      request.get('/category/selectAll').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data || []
        }
      }).catch(() => {})
    },

    /* ---------- 目录 ---------- */
    buildToc() {
      const container = this.$refs.content
      if (!container) {
        return
      }
      const headings = container.querySelectorAll('h1, h2, h3')
      const items = []
      let minLevel = 6
      headings.forEach((el, index) => {
        const level = Number(el.tagName.substring(1))
        el.id = 'md-heading-' + index
        minLevel = Math.min(minLevel, level)
        items.push({
          id: el.id,
          text: el.textContent.trim(),
          level
        })
      })
      items.forEach(item => {
        item.indent = item.level - minLevel
      })
      this.tocItems = items
      this.setupObserver(headings)
    },
    setupObserver(headings) {
      if (typeof IntersectionObserver === 'undefined' ||
          headings.length === 0) {
        return
      }
      this.observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.activeHeading = entry.target.id
          }
        })
      }, {
        rootMargin: '0px 0px -70% 0px'
      })
      headings.forEach(el => this.observer.observe(el))
    },
    disconnectObserver() {
      if (this.observer) {
        this.observer.disconnect()
        this.observer = null
      }
    },
    scrollToHeading(id) {
      const el = document.getElementById(id)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' })
        this.activeHeading = id
      }
    },

    /* ---------- 代码复制 ---------- */
    onContentClick(event) {
      const button = event.target.closest('.code-copy-btn')
      if (!button) {
        return
      }
      const block = button.closest('.code-block')
      const code = block && block.querySelector('code')
      if (!code) {
        return
      }
      this.copyText(code.innerText).then(() => {
        button.textContent = '已复制'
        setTimeout(() => {
          button.textContent = '复制'
        }, 1500)
      }).catch(() => {
        this.$message.error('复制失败，请手动选择复制')
      })
    },
    copyText(text) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        return navigator.clipboard.writeText(text)
      }
      return new Promise((resolve, reject) => {
        const textarea = document.createElement('textarea')
        textarea.value = text
        textarea.style.position = 'fixed'
        textarea.style.opacity = '0'
        document.body.appendChild(textarea)
        textarea.select()
        try {
          document.execCommand('copy') ? resolve() : reject(new Error())
        } catch (e) {
          reject(e)
        } finally {
          document.body.removeChild(textarea)
        }
      })
    },

    /* ---------- 时间格式化 ---------- */
    formatTime(value) {
      return formatDateTime(value) || '未知日期'
    }
  }
}
</script>

<style scoped>
.detail-page {
  min-height: calc(100vh - 120px);
  padding: 28px 20px 64px;
  color: #1f1e33;
  /* 文章阅读区在前台 1.1 基础上再放大 1.1（净约 1.21 倍），正文更好读 */
  zoom: 1.1;
}

.detail-layout {
  display: flex;
  justify-content: center;
  gap: 36px;
  max-width: 1100px;
  margin: 0 auto;
}

.detail-content {
  flex: 1;
  min-width: 0;
  max-width: 820px;
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
  align-items: center;
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

.markdown-body {
  margin-top: 30px;
}

.cover-hero {
  display: block;
  width: 100%;
  max-height: 360px;
  object-fit: cover;
  border-radius: 10px;
  margin-top: 24px;
  border: 1px solid #ebeef5;
}

/* ---------- 目录 ---------- */
.toc {
  width: 220px;
  flex-shrink: 0;
}

.toc-inner {
  position: sticky;
  top: 24px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 4px 0 4px 16px;
  border-left: 1px solid #ebeef5;
}

.toc-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  letter-spacing: 2px;
}

.toc ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.toc-item {
  padding: 4px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
  cursor: pointer;
  transition: color 0.2s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toc-item:hover {
  color: #409eff;
}

.toc-item.active {
  color: #409eff;
  font-weight: 600;
}

.toc-level-1 {
  padding-left: 14px;
}

.toc-level-2 {
  padding-left: 28px;
}

.toc-level-3 {
  padding-left: 42px;
}

@media (max-width: 1100px) {
  .toc {
    display: none;
  }
}

/* ---------- 上一篇 / 下一篇 ---------- */
.post-nav {
  display: flex;
  gap: 16px;
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.post-nav-card {
  flex: 1;
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  text-decoration: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.post-nav-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12);
}

.post-nav-card--next {
  text-align: right;
}

.post-nav-placeholder {
  flex: 1;
}

.post-nav-label {
  display: block;
  font-size: 12px;
  color: #909399;
}

.post-nav-title {
  display: block;
  margin-top: 6px;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
