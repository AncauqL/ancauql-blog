<template>
  <div>
    <!-- ======== Hero 区域 ======== -->
    <section class="pt-32 pb-20 md:pt-44 md:pb-28 px-6">
      <div class="max-w-7xl mx-auto">
        <div class="max-w-3xl">
          <div class="animate-in flex items-center gap-2 mb-6">
            <div class="w-2 h-2 rounded-full bg-neutral-900"></div>
            <span class="text-xs font-medium uppercase tracking-widest text-neutral-400">个人博客</span>
          </div>
          <h1 class="animate-in delay-100 text-4xl md:text-6xl lg:text-7xl font-semibold tracking-tight leading-[1.1] text-neutral-900">
            思考，记录，<br>
            <span class="text-neutral-300">然后遗忘。</span>
          </h1>
          <p class="animate-in delay-200 mt-6 md:mt-8 text-base md:text-lg font-light leading-relaxed text-neutral-500 max-w-xl">
            {{ site.heroText }}
          </p>
          <div class="animate-in delay-300 mt-8 flex items-center gap-4">
            <a href="#posts" class="inline-flex items-center gap-2 bg-neutral-900 text-white text-sm font-medium px-6 h-11 rounded-full hover:bg-neutral-700 transition-colors duration-300">
              阅读文章
              <icon-arrow-down :width="15" />
            </a>
            <router-link to="/aboutme" class="inline-flex items-center gap-2 text-sm font-medium text-neutral-500 hover:text-neutral-900 px-4 h-11 rounded-full border border-neutral-200 hover:border-neutral-400 transition-all duration-300">
              了解更多
            </router-link>
          </div>
        </div>

        <!-- 统计 -->
        <div class="animate-in delay-400 mt-16 md:mt-24 flex gap-10 md:gap-16">
          <div>
            <div class="text-2xl md:text-3xl font-semibold tracking-tight">{{ stats.articleCount }}</div>
            <div class="text-xs text-neutral-400 mt-1 uppercase tracking-wider">篇文章</div>
          </div>
          <div>
            <div class="text-2xl md:text-3xl font-semibold tracking-tight">{{ stats.viewText }}</div>
            <div class="text-xs text-neutral-400 mt-1 uppercase tracking-wider">总阅读</div>
          </div>
          <div>
            <div class="text-2xl md:text-3xl font-semibold tracking-tight">{{ stats.years }}</div>
            <div class="text-xs text-neutral-400 mt-1 uppercase tracking-wider">年写作</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ======== 精选文章 ======== -->
    <section v-if="featured" class="px-6 pb-20 md:pb-28">
      <div class="max-w-7xl mx-auto">
        <router-link :to="'/post/' + featured.id" class="group block relative overflow-hidden rounded-2xl bg-neutral-900 aspect-[21/9] md:aspect-[3/1]">
          <img
              v-if="featured.cover"
              :src="resolveAsset(featured.cover)"
              alt=""
              class="absolute inset-0 w-full h-full object-cover opacity-50 group-hover:opacity-40 group-hover:scale-105 transition-all duration-700"
          >
          <div class="relative z-10 h-full flex flex-col justify-end p-6 md:p-12">
            <div class="flex items-center gap-2 mb-3">
              <span class="text-[10px] font-semibold uppercase tracking-widest text-white/60 bg-white/10 backdrop-blur-sm px-2.5 py-1 rounded-full">精选</span>
              <span class="text-[10px] font-medium text-white/40">{{ formatTime(featured.createTime) }}</span>
            </div>
            <h2 class="text-xl md:text-3xl lg:text-4xl font-semibold text-white tracking-tight leading-tight max-w-2xl">
              {{ featured.title }}
            </h2>
            <p v-if="featured.summary" class="mt-2 text-sm text-white/50 font-light max-w-lg hidden md:block">
              {{ featured.summary }}
            </p>
            <div class="mt-4 flex items-center gap-2 text-white/60 group-hover:text-white/90 transition-colors text-sm font-medium">
              阅读全文
              <icon-arrow-right :width="15" class="group-hover:translate-x-1 transition-transform duration-200" />
            </div>
          </div>
        </router-link>
      </div>
    </section>

    <!-- ======== 文章列表 ======== -->
    <section id="posts" class="px-6 pb-20 md:pb-32">
      <div class="max-w-7xl mx-auto">
        <!-- 标题与筛选 -->
        <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-10">
          <div>
            <div class="h-px bg-neutral-200 mb-6 divider-line" :class="{ visible: postsDividerVisible }"></div>
            <h2 class="text-2xl md:text-3xl font-semibold tracking-tight">最新文章</h2>
          </div>
          <div class="flex items-center gap-2 flex-wrap">
            <button
                :class="['tag-btn text-xs font-medium px-3.5 py-1.5 rounded-full border border-neutral-200', activeCategoryId === null ? 'active' : 'text-neutral-500']"
                @click="selectCategory(null)"
            >全部</button>
            <button
                v-for="item in categoryList"
                :key="item.id"
                :class="['tag-btn text-xs font-medium px-3.5 py-1.5 rounded-full border border-neutral-200', activeCategoryId === item.id ? 'active' : 'text-neutral-500']"
                @click="selectCategory(item.id)"
            >{{ item.name }}</button>
          </div>
        </div>

        <!-- 文章网格 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <article
              v-for="item in articleList"
              :key="item.id"
              class="post-card group"
          >
            <router-link :to="'/post/' + item.id" class="block">
              <div class="aspect-[4/3] rounded-xl overflow-hidden bg-neutral-100">
                <img
                    v-if="item.cover"
                    :src="resolveAsset(item.cover)"
                    alt=""
                    class="w-full h-full object-cover"
                >
              </div>
              <div class="pt-4">
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-[10px] font-semibold uppercase tracking-widest text-neutral-400">{{ categoryName(item.categoryId) }}</span>
                  <span class="text-neutral-300">·</span>
                  <span class="text-[10px] text-neutral-400">{{ formatTime(item.createTime).slice(5) }}</span>
                </div>
                <h3 class="text-base font-semibold tracking-tight leading-snug group-hover:text-neutral-600 transition-colors">
                  {{ item.title }}
                </h3>
                <p class="mt-2 text-sm text-neutral-400 font-light leading-relaxed line-clamp-2">
                  {{ item.summary }}
                </p>
              </div>
            </router-link>
          </article>
        </div>

        <el-empty
            v-if="loaded && articleList.length === 0"
            description="暂无文章"
        />

        <!-- 加载更多 -->
        <div v-if="hasMore" class="mt-12 text-center">
          <button
              :disabled="loadingMore"
              class="inline-flex items-center gap-2 text-sm font-medium text-neutral-500 hover:text-neutral-900 px-6 h-11 rounded-full border border-neutral-200 hover:border-neutral-400 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="loadMore"
          >
            {{ loadMoreText }}
            <icon-chevron-down v-if="!loadingMore" :width="15" />
          </button>
        </div>
        <div v-else-if="loaded && articleList.length > 0" class="mt-12 text-center text-sm text-neutral-400">
          已加载全部文章
        </div>
      </div>
    </section>

    <!-- ======== 关于我 ======== -->
    <section id="about" class="px-6 pb-20 md:pb-24">
      <div class="max-w-7xl mx-auto">
        <div class="grid grid-cols-1 lg:grid-cols-5 gap-12 lg:gap-20 items-center">
          <!-- 图片（没设肖像图就用站名首字母占位，不会裂图） -->
          <div class="lg:col-span-2">
            <img
                v-if="site.portrait && !portraitFailed"
                :src="resolveAsset(site.portrait)"
                alt="作者"
                class="aspect-[3/4] w-full rounded-2xl object-cover grayscale hover:grayscale-0 transition-all duration-700"
                @error="onPortraitError"
            >
            <div v-else class="aspect-[3/4] w-full rounded-2xl bg-neutral-100 flex items-center justify-center select-none">
              <span class="font-semibold text-neutral-300" style="font-size:112px;line-height:1">{{ initials }}</span>
            </div>
          </div>
          <!-- 文字 -->
          <div class="lg:col-span-3">
            <div class="h-px bg-neutral-200 mb-6 divider-line" :class="{ visible: aboutDividerVisible }"></div>
            <h2 class="text-2xl md:text-3xl font-semibold tracking-tight mb-6">关于我</h2>
            <div class="space-y-4 text-sm md:text-base text-neutral-500 font-light leading-relaxed">
              <p v-for="(line, i) in site.aboutLines" :key="'about-' + i">{{ line }}</p>
            </div>
            <div class="mt-6">
              <router-link to="/aboutme" class="inline-flex items-center gap-1.5 text-sm font-medium text-neutral-900 hover:text-neutral-500 transition-colors">
                完整介绍
                <icon-arrow-right :width="14" />
              </router-link>
            </div>
            <div class="mt-8 flex items-center gap-4">
              <a v-if="site.socials.github" :href="site.socials.github" target="_blank" rel="noopener noreferrer" class="w-10 h-10 rounded-full border border-neutral-200 flex items-center justify-center text-neutral-400 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300" aria-label="GitHub">
                <icon-github :width="17" />
              </a>
              <a v-if="site.socials.bilibili" :href="site.socials.bilibili" target="_blank" rel="noopener noreferrer" class="w-10 h-10 rounded-full border border-neutral-200 flex items-center justify-center text-neutral-400 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300" aria-label="Bilibili">
                <icon-bilibili :width="17" />
              </a>
              <a v-if="site.socials.email" :href="'mailto:' + site.socials.email" class="w-10 h-10 rounded-full border border-neutral-200 flex items-center justify-center text-neutral-400 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300" aria-label="邮箱">
                <icon-mail :width="17" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ======== 保持联系（RSS + 社交 + 写信；不再做“假订阅”） ======== -->
    <section id="contact" class="px-6 pb-20 md:pb-32">
      <div class="max-w-7xl mx-auto">
        <div class="bg-neutral-50 rounded-2xl p-8 md:p-16 text-center">
          <div class="h-px bg-neutral-200 mb-6 divider-line mx-auto" :class="{ visible: subscribeDividerVisible }"></div>
          <h2 class="text-2xl md:text-3xl font-semibold tracking-tight">保持联系</h2>
          <p class="mt-3 text-sm md:text-base text-neutral-500 font-light max-w-md mx-auto">
            想看新文章？订阅 RSS；想聊聊，给我写信，或到这些地方找我。
          </p>
          <div class="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
            <a
                :href="feedUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="inline-flex items-center gap-2 h-11 px-6 bg-neutral-900 text-white text-sm font-medium rounded-full hover:bg-neutral-700 transition-colors duration-300 whitespace-nowrap"
            >
              <icon-rss :width="15" /> RSS 订阅
            </a>
            <div class="flex items-center gap-2.5">
              <a v-if="site.socials.github" :href="site.socials.github" target="_blank" rel="noopener noreferrer" class="w-11 h-11 rounded-full border border-neutral-200 flex items-center justify-center text-neutral-500 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300" aria-label="GitHub">
                <icon-github :width="18" />
              </a>
              <a v-if="site.socials.bilibili" :href="site.socials.bilibili" target="_blank" rel="noopener noreferrer" class="w-11 h-11 rounded-full border border-neutral-200 flex items-center justify-center text-neutral-500 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300" aria-label="Bilibili">
                <icon-bilibili :width="18" />
              </a>
              <a v-if="site.socials.email" :href="'mailto:' + site.socials.email" class="w-11 h-11 rounded-full border border-neutral-200 flex items-center justify-center text-neutral-500 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300" aria-label="给我写信">
                <icon-mail :width="18" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import request, { resolveAsset, API_BASE } from '@/utils/request'
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
      total: 0,
      loadingMore: false,
      stats: {
        articleCount: 0,
        viewText: '0',
        years: 0
      },
      featured: null,
      postsDividerVisible: false,
      aboutDividerVisible: false,
      subscribeDividerVisible: false,
      portraitFailed: false
    }
  },
  computed: {
    hasMore() {
      return this.articleList.length < this.total
    },
    loadMoreText() {
      return this.loadingMore ? '加载中…' : '加载更多'
    },
    initials() {
      const name = (this.site && this.site.name) || 'A'
      return name.trim().charAt(0).toUpperCase()
    },
    feedUrl() {
      return API_BASE + '/feed.xml'
    }
  },
  created() {
    this.loadCategories()
    this.load()
    this.loadStats()
  },
  mounted() {
    this.observeDividers()
  },
  beforeDestroy() {
    if (this.observer) {
      this.observer.disconnect()
    }
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
    loadStats() {
      request.get('/article/stats').then(res => {
        if (res.code === '200' && res.data) {
          const views = res.data.totalViews || 0
          this.stats.articleCount = res.data.articleCount || 0
          this.stats.viewText = views >= 10000
              ? (views / 10000).toFixed(1) + '万'
              : String(views)
        }
      }).catch(() => {})
      const years = new Date().getFullYear() - this.site.startYear
      this.stats.years = Math.max(years, 0)
    },
    selectCategory(id) {
      this.activeCategoryId = id
      this.pageNum = 1
      this.load()
    },
    loadMore() {
      this.loadingMore = true
      const params = {
        pageNum: this.pageNum + 1,
        pageSize: this.pageSize,
        status: 'published'
      }
      if (this.activeCategoryId !== null) {
        params.categoryId = this.activeCategoryId
      }
      request.get('/article/selectPage', { params }).then(res => {
        if (res.code === '200' && res.data) {
          this.pageNum += 1
          this.articleList = this.articleList.concat(res.data.records || [])
          this.total = res.data.total || 0
        } else {
          this.$message.error(res.msg)
        }
        this.loadingMore = false
      }).catch(() => {
        this.loadingMore = false
        this.$message.error('加载失败，请稍后重试')
      })
    },
    categoryName(categoryId) {
      const category = this.categoryList.find(item => item.id === categoryId)
      return category ? category.name : '未分类'
    },
    observeDividers() {
      // 分割线进入视口时展开；精选文章取列表第一篇（服务端已按时间倒序）
      this.observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            if (entry.target.closest('#posts')) {
              this.postsDividerVisible = true
            } else if (entry.target.closest('#about')) {
              this.aboutDividerVisible = true
            } else if (entry.target.closest('#contact')) {
              this.subscribeDividerVisible = true
            }
          }
        })
      }, { threshold: 0.5 })
      this.$el.querySelectorAll('.divider-line:not(.visible)').forEach(el => {
        this.observer.observe(el)
      })
      if (this.articleList.length > 0) {
        this.featured = this.articleList[0]
      }
    },
    onPortraitError() {
      this.portraitFailed = true
    },
    formatTime(value) {
      if (!value) {
        return '未知日期'
      }
      if (Array.isArray(value)) {
        return `${value[0]}-${String(value[1]).padStart(2, '0')}-${String(value[2]).padStart(2, '0')}`
      }
      return String(value).replace('T', ' ').slice(0, 10)
    }
  },
  watch: {
    articleList(list) {
      // 首页加载后或筛选后，第一篇作为精选大卡
      if (list && list.length > 0 && this.pageNum === 1) {
        this.featured = list[0]
      }
    }
  }
}
</script>

<style scoped>
/* 文章卡片悬停图片 */
.post-card img {
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.post-card:hover img {
  transform: scale(1.05);
}

/* 标签悬停 */
.tag-btn {
  transition: all 0.25s ease;
}
.tag-btn.active {
  background: #0a0a0a;
  color: #fff;
}
.tag-btn:not(.active):hover {
  background: #f5f5f5;
}

/* 分割线动画 */
.divider-line {
  width: 0;
  transition: width 0.8s ease;
}
.divider-line.visible {
  width: 48px;
}
</style>
