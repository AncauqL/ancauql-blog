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

        <!-- ======== 评论区（登录后可评论） ======== -->
        <section class="comments">
          <h2 class="comments-title">
            评论
            <span v-if="comments.length" class="comments-count">({{ comments.length }})</span>
          </h2>

          <div v-if="comments.length" class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <div class="comment-head">
                <span class="comment-name">{{ c.nickname }}</span>
                <span class="comment-time">{{ formatTime(c.createTime) }}</span>
                <button
                    v-if="canDelete(c)"
                    type="button"
                    class="comment-del"
                    @click="removeComment(c.id)"
                >删除</button>
              </div>
              <div class="comment-text">{{ c.content }}</div>
            </div>
          </div>
          <p v-else class="comments-empty">还没有评论，来抢个沙发～</p>

          <!-- 未登录：提示登录 -->
          <div v-if="!currentUser" class="comment-login-tip">
            登录后可发表评论
            <a class="comment-login-link" @click="goLogin">去登录</a>
          </div>

          <!-- 已登录：直接发表（昵称取账号） -->
          <form v-else class="comment-form" @submit.prevent="submitComment">
            <!-- 蜜罐：视觉隐藏，只有灌水机器人会填 -->
            <input
                v-model="commentWebsite"
                type="text"
                class="comment-honeypot"
                name="website"
                tabindex="-1"
                autocomplete="off"
            >
            <div class="comment-as">以「{{ displayName }}」评论</div>
            <textarea
                v-model="commentText"
                class="comment-textarea"
                placeholder="说点什么…（最多 2000 字）"
                maxlength="2000"
                rows="4"
                required
            ></textarea>
            <div class="comment-actions">
              <span class="comment-hint">理性发言，友善交流</span>
              <button type="submit" class="comment-submit" :disabled="submitting">
                {{ submitting ? '提交中…' : '发表评论' }}
              </button>
            </div>
          </form>
        </section>
      </article>

      <aside v-if="tocOpen && tocItems.length >= 2" class="toc">
        <div class="toc-inner">
          <div class="toc-head">
            <span class="toc-title">目录</span>
            <button type="button" class="toc-hide-btn" @click="tocOpen = false">收起</button>
          </div>
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

    <button
        v-if="hasToc"
        type="button"
        class="toc-toggle"
        :title="tocOpen ? '收起目录' : '展开目录'"
        :aria-label="tocOpen ? '收起目录' : '展开目录'"
        @click="tocOpen = !tocOpen"
    >
      <icon-toc v-if="!tocOpen" :width="18" />
      <icon-x v-else :width="16" />
    </button>

    <el-empty
        v-else-if="loaded"
        :description="errorText"
    />
  </section>
</template>

<script>
import request from '@/utils/request'
import { renderMarkdown, countWords, readingMinutes } from '@/utils/markdown'
import { formatDateTime } from '@/utils/datetime'
import { getStoredUser } from '@/utils/auth'

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
      observer: null,
      tocOpen: false,
      comments: [],
      currentUser: getStoredUser(),
      commentText: '',
      commentWebsite: '',
      submitting: false
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
    displayName() {
      const u = this.currentUser
      if (!u) {
        return ''
      }
      return (u.nickname && u.nickname.trim()) ? u.nickname : u.username
    },
    categoryName() {
      if (!this.article || !this.article.categoryId) {
        return ''
      }
      const category = this.categoryList.find(
          item => item.id === this.article.categoryId)
      return category ? category.name : ''
    },
    hasToc() {
      return this.tocItems.length >= 2
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
    reset() {
      this.disconnectObserver()
      this.article = null
      this.loaded = false
      this.errorText = '文章不存在'
      this.neighbors = { prev: null, next: null }
      this.tocItems = []
      this.activeHeading = ''
      this.comments = []
      this.commentText = ''
      this.commentWebsite = ''
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
          this.loadComments(id)
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

    /* ---------- 评论 ---------- */
    loadComments(id) {
      request.get('/comment', { params: { articleId: id } }).then(res => {
        if (res.code === '200') {
          this.comments = res.data || []
        }
      }).catch(() => {})
    },
    submitComment() {
      const text = this.commentText.trim()
      if (!text) {
        return
      }
      this.submitting = true
      request.post('/comment', {
        articleId: this.article.id,
        content: text,
        website: this.commentWebsite
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('评论成功')
          this.commentText = ''
          this.commentWebsite = ''
          this.loadComments(this.article.id)
        } else {
          this.$message.error(res.msg || '发表失败')
        }
      }).catch(() => {
        this.$message.error('网络错误，请稍后再试')
      }).finally(() => {
        this.submitting = false
      })
    },
    canDelete(c) {
      const u = this.currentUser
      if (!u) {
        return false
      }
      if (u.role === 'SUPER_ADMIN' || u.role === 'ADMIN') {
        return true
      }
      return !!c.userId && Number(c.userId) === Number(u.id)
    },
    removeComment(id) {
      this.$confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
        .then(() => {
          request.delete('/comment/delete?id=' + id).then(res => {
            if (res.code === '200') {
              this.$message.success('已删除')
              this.loadComments(this.article.id)
            } else {
              this.$message.error(res.msg || '删除失败')
            }
          })
        }).catch(() => {})
    },
    goLogin() {
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
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

/* 目录可收起：右侧浮动开关（宽屏才显示） */
.toc-toggle {
  position: fixed;
  top: 84px;
  right: 16px;
  z-index: 30;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #4b5563;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07);
  transition: color 0.2s ease, border-color 0.2s ease;
}
.toc-toggle:hover {
  color: #1f1e33;
  border-color: #1f1e33;
}

/* 目录头部行（标题 + 收起按钮） */
.toc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.toc-head .toc-title {
  margin-bottom: 0;
}
.toc-hide-btn {
  border: none;
  background: transparent;
  padding: 2px 4px;
  color: #a0a3a8;
  font-size: 12px;
  cursor: pointer;
}
.toc-hide-btn:hover {
  color: #1f1e33;
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

/* ---------- 评论区 ---------- */
.comments {
  margin-top: 44px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
.comments-title {
  margin: 0 0 16px;
  font-size: 20px;
  color: #1f1e33;
}
.comments-count {
  color: #909399;
  font-weight: normal;
  font-size: 14px;
}
.comment-list {
  margin-bottom: 20px;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px dashed #f0f1f3;
}
.comment-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}
.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f1e33;
}
.comment-time {
  font-size: 12px;
  color: #a0a3a8;
}
.comment-text {
  margin-top: 6px;
  font-size: 15px;
  line-height: 1.8;
  color: #444b52;
  white-space: pre-wrap;
  word-break: break-word;
}
.comments-empty {
  margin: 0 0 20px;
  color: #909399;
  font-size: 14px;
}
.comment-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.comment-honeypot {
  position: absolute;
  left: -9999px;
  top: -9999px;
  width: 1px;
  height: 1px;
  opacity: 0;
}
.comment-name-input {
  height: 38px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  max-width: 260px;
}
.comment-name-input:focus,
.comment-textarea:focus {
  border-color: #1f1e33;
}
.comment-textarea {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.7;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.comment-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.comment-hint {
  color: #a0a3a8;
  font-size: 12px;
}
.comment-submit {
  height: 36px;
  padding: 0 20px;
  border: none;
  border-radius: 6px;
  background: #1f1e33;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}
.comment-submit:hover {
  background: #545c64;
}
.comment-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.comment-as {
  font-size: 12px;
  color: #909399;
}

.comment-del {
  margin-left: auto;
  border: none;
  background: transparent;
  color: #c0c4cc;
  font-size: 12px;
  cursor: pointer;
}
.comment-del:hover {
  color: #f56c6c;
}

.comment-login-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  color: #606266;
  font-size: 14px;
}
.comment-login-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}
.comment-login-link:hover {
  text-decoration: underline;
}

@media (max-width: 1100px) {
  .toc {
    display: none;
  }
  .toc-toggle {
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
