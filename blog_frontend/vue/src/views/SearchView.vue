<template>
  <section class="search-page">
    <div class="search-wrap">
      <router-link to="/" class="back-link">← 返回首页</router-link>

      <h1 class="search-title">
        <template v-if="keyword">搜索「{{ keyword }}」</template>
        <template v-else>搜索文章</template>
      </h1>

      <div class="search-bar">
        <icon-search :width="17" class="search-icon" />
        <input
            ref="input"
            v-model="input"
            type="text"
            placeholder="输入关键词，回车搜索（标题 / 摘要 / 正文）"
            class="search-input"
            @keyup.enter="submit"
        >
        <button class="search-btn" @click="submit">搜索</button>
      </div>

      <div v-if="!keyword" class="hint">
        试试搜作者写过的技术名词、书名或生活片段。
      </div>

      <template v-else>
        <div v-if="loaded" class="result-count">
          共找到 <span class="count">{{ total }}</span> 篇相关文章
        </div>

        <div class="result-list">
          <router-link
              v-for="item in articleList"
              :key="item.id"
              :to="'/post/' + item.id"
              class="result-item"
          >
            <h2 class="result-title">
              <template v-for="(part, i) in segments(item.title)">
                <span v-if="part.hit" :key="'t' + i" class="hit">{{ part.text }}</span>
                <template v-else>{{ part.text }}</template>
              </template>
            </h2>
            <p v-if="item.summary" class="result-summary">
              <template v-for="(part, i) in segments(item.summary)">
                <span v-if="part.hit" :key="'s' + i" class="hit">{{ part.text }}</span>
                <template v-else>{{ part.text }}</template>
              </template>
            </p>
            <div class="result-meta">
              <span>{{ categoryName(item.categoryId) }}</span>
              <span class="dot">·</span>
              <span>{{ formatTime(item.createTime) }}</span>
              <span v-if="item.tagNames && item.tagNames.length" class="dot">·</span>
              <span
                  v-for="name in (item.tagNames || [])"
                  :key="'tag-' + item.id + '-' + name"
                  class="tag"
              >#{{ name }}</span>
            </div>
          </router-link>
        </div>

        <el-empty
            v-if="loaded && articleList.length === 0"
            description="没有找到相关文章，换个关键词试试"
        />

        <div v-if="hasMore" class="more-wrap">
          <button :disabled="loadingMore" class="more-btn" @click="loadMore">
            {{ loadingMore ? '加载中…' : '加载更多' }}
          </button>
        </div>
        <div v-else-if="loaded && articleList.length > 0" class="more-wrap muted">
          已显示全部结果
        </div>
      </template>
    </div>
  </section>
</template>

<script>
import request from '@/utils/request'
import { formatDate } from '@/utils/datetime'
import { setSeo } from '@/utils/seo'

export default {
  name: 'SearchView',
  data() {
    return {
      input: '',
      keyword: '',
      articleList: [],
      categoryList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loaded: false,
      loadingMore: false,
      requestId: 0
    }
  },
  computed: {
    hasMore() {
      return this.articleList.length < this.total
    }
  },
  created() {
    this.loadCategories()
    this.syncFromQuery()
    this.load()
    this.applySeo()
  },
  methods: {
    /** 搜索结果页不进搜索引擎索引 */
    applySeo() {
      setSeo({
        title: this.keyword ? `搜索「${this.keyword}」` : '搜索',
        description: '站内搜索：按关键词查找文章。',
        path: '/search',
        noindex: true
      })
    },
    syncFromQuery() {
      const q = this.$route.query.q
      this.keyword = typeof q === 'string' ? q.trim() : ''
      this.input = this.keyword
    },
    submit() {
      const q = this.input.trim()
      const query = q ? { q } : {}
      if (q === this.keyword) {
        // 关键词没变也要能重搜（比如上次失败了）
        this.reload()
        return
      }
      this.$router.push({ path: '/search', query }).catch(() => {})
    },
    reload() {
      this.pageNum = 1
      this.articleList = []
      this.load()
    },
    load() {
      if (!this.keyword) {
        this.loaded = true
        this.total = 0
        this.articleList = []
        return
      }
      this.loaded = false
      // 快速连打时丢弃过期响应
      const rid = ++this.requestId
      request.get('/article/search', {
        params: { keyword: this.keyword, pageNum: this.pageNum, pageSize: this.pageSize }
      }).then(res => {
        if (rid !== this.requestId) {
          return
        }
        this.loaded = true
        if (res.code === '200') {
          this.articleList = (res.data && res.data.records) || []
          this.total = (res.data && res.data.total) || 0
        } else {
          this.$message.error(res.msg || '搜索失败')
        }
      }).catch(() => {
        if (rid !== this.requestId) {
          return
        }
        this.loaded = true
        this.$message.error('搜索失败，请确认后端已启动')
      })
    },
    loadMore() {
      if (this.loadingMore) {
        return
      }
      this.loadingMore = true
      const rid = this.requestId
      request.get('/article/search', {
        params: {
          keyword: this.keyword,
          pageNum: this.pageNum + 1,
          pageSize: this.pageSize
        }
      }).then(res => {
        if (rid === this.requestId && res.code === '200' && res.data) {
          this.pageNum += 1
          this.articleList = this.articleList.concat(res.data.records || [])
          this.total = res.data.total || 0
        }
      }).catch(() => {
        this.$message.error('加载失败，请稍后重试')
      }).finally(() => {
        this.loadingMore = false
      })
    },
    loadCategories() {
      request.get('/category/selectAll').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data || []
        }
      }).catch(() => {})
    },
    categoryName(categoryId) {
      const category = this.categoryList.find(item => item.id === categoryId)
      return category ? category.name : '未分类'
    },
    formatTime(value) {
      return formatDate(value) || '未知日期'
    },
    /** 把命中关键词的片段切出来单独高亮（不用 v-html，避免 XSS） */
    segments(text) {
      const source = text || ''
      const kw = this.keyword
      if (!kw) {
        return [{ text: source, hit: false }]
      }
      const lower = source.toLowerCase()
      const target = kw.toLowerCase()
      const parts = []
      let index = 0
      while (index < source.length) {
        const found = lower.indexOf(target, index)
        if (found === -1) {
          parts.push({ text: source.slice(index), hit: false })
          break
        }
        if (found > index) {
          parts.push({ text: source.slice(index, found), hit: false })
        }
        parts.push({ text: source.slice(found, found + kw.length), hit: true })
        index = found + kw.length
      }
      return parts
    }
  },
  watch: {
    '$route.query.q'() {
      this.syncFromQuery()
      this.reload()
      this.applySeo()
    }
  }
}
</script>

<style scoped>
.search-page {
  padding: 56px 24px 96px;
}

.search-wrap {
  max-width: 760px;
  margin: 0 auto;
}

.back-link {
  font-size: 13px;
  color: #a3a3a3;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: #0a0a0a;
}

.search-title {
  margin: 18px 0 22px;
  font-size: 30px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: #171717;
  word-break: break-word;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 52px;
  padding: 0 16px;
  border: 1px solid #e5e5e5;
  border-radius: 9999px;
  transition: border-color 0.25s ease;
}

.search-bar:focus-within {
  border-color: #a3a3a3;
}

.search-icon {
  color: #a3a3a3;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}

.search-btn {
  flex-shrink: 0;
  height: 34px;
  padding: 0 18px;
  border-radius: 9999px;
  background: #0a0a0a;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  transition: opacity 0.2s ease;
}

.search-btn:hover {
  opacity: 0.85;
}

.hint {
  margin-top: 28px;
  font-size: 14px;
  color: #a3a3a3;
}

.result-count {
  margin: 28px 0 6px;
  font-size: 13px;
  color: #a3a3a3;
}

.result-count .count {
  color: #171717;
  font-weight: 600;
}

.result-list {
  margin-top: 8px;
}

.result-item {
  display: block;
  padding: 22px 0;
  border-bottom: 1px solid #f5f5f5;
}

.result-item:hover .result-title {
  color: #525252;
}

.result-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: #171717;
  line-height: 1.5;
  transition: color 0.2s ease;
}

.result-summary {
  margin: 8px 0 0;
  font-size: 14px;
  font-weight: 300;
  line-height: 1.75;
  color: #737373;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 11px;
  color: #a3a3a3;
}

.result-meta .dot {
  color: #d4d4d4;
}

.result-meta .tag {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 9999px;
  padding: 1px 8px;
}

.hit {
  background: #fff3c4;
  color: #171717;
  border-radius: 3px;
  padding: 0 1px;
}

.more-wrap {
  margin-top: 32px;
  text-align: center;
}

.more-wrap.muted {
  font-size: 13px;
  color: #a3a3a3;
}

.more-btn {
  height: 42px;
  padding: 0 28px;
  border: 1px solid #e5e5e5;
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 500;
  color: #525252;
  transition: all 0.25s ease;
}

.more-btn:hover:not(:disabled) {
  border-color: #a3a3a3;
  color: #0a0a0a;
}

.more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .search-page {
    padding: 36px 18px 72px;
  }

  .search-title {
    font-size: 24px;
  }
}
</style>
