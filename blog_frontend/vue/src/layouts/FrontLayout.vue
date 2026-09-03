<template>
  <div class="front-layout bg-white text-neutral-900 font-sans antialiased">
    <!-- ======== 导航栏 ======== -->
    <nav id="navbar" ref="navbar" class="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-md border-b border-transparent transition-all duration-300">
      <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2.5 group">
          <div class="w-8 h-8 bg-neutral-900 rounded-lg flex items-center justify-center group-hover:bg-neutral-700 transition-colors duration-300">
            <img src="/my_logo.svg" alt="logo" class="w-5 h-5 object-contain">
          </div>
          <span class="text-base font-semibold tracking-tight">{{ site.name }}</span>
        </router-link>

        <!-- 桌面导航 -->
        <div class="hidden md:flex items-center gap-8">
          <router-link to="/#posts" class="text-sm text-neutral-500 hover:text-neutral-900 transition-colors duration-200">文章</router-link>
          <router-link to="/archive" class="text-sm text-neutral-500 hover:text-neutral-900 transition-colors duration-200">归档</router-link>
          <router-link to="/aboutme" class="text-sm text-neutral-500 hover:text-neutral-900 transition-colors duration-200">关于</router-link>
          <button @click="openSearch" class="text-neutral-400 hover:text-neutral-900 transition-colors duration-200" aria-label="搜索">
            <icon-search :width="18" />
          </button>
          <!-- 极简账号入口：游客登录；管理员进后台 -->
          <template v-if="currentUser">
            <router-link
              v-if="manager"
              to="/article"
              class="text-sm text-neutral-500 hover:text-neutral-900 transition-colors duration-200"
            >进入后台</router-link>
            <a
              href="javascript:void(0)"
              class="text-sm text-neutral-500 hover:text-neutral-900 transition-colors duration-200"
              @click="doLogout"
            >退出</a>
          </template>
          <a
            v-else
            href="javascript:void(0)"
            class="text-sm text-neutral-500 hover:text-neutral-900 transition-colors duration-200"
            @click="goLogin"
          >登录</a>
        </div>

        <!-- 移动端按钮 -->
        <div class="flex md:hidden items-center gap-3">
          <button @click="openSearch" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="搜索">
            <icon-search :width="18" />
          </button>
          <button @click="toggleMobileMenu" class="text-neutral-900" aria-label="菜单">
            <icon-menu :width="22" />
          </button>
        </div>
      </div>
    </nav>

    <!-- ======== 移动端菜单 ======== -->
    <div id="mobile-menu" class="mobile-menu fixed inset-0 z-[60] bg-white">
      <div class="px-6 h-16 flex items-center justify-between border-b border-neutral-100">
        <span class="text-base font-semibold tracking-tight">{{ site.name }}</span>
        <button @click="toggleMobileMenu" aria-label="关闭菜单">
          <icon-x :width="22" />
        </button>
      </div>
      <div class="px-6 py-8 flex flex-col gap-6">
        <router-link to="/#posts" @click.native="closeMobileMenu" class="text-2xl font-semibold tracking-tight hover:text-neutral-500 transition-colors">文章</router-link>
        <router-link to="/archive" @click.native="closeMobileMenu" class="text-2xl font-semibold tracking-tight hover:text-neutral-500 transition-colors">归档</router-link>
        <router-link to="/aboutme" @click.native="closeMobileMenu" class="text-2xl font-semibold tracking-tight hover:text-neutral-500 transition-colors">关于</router-link>
        <template v-if="currentUser">
          <router-link
            v-if="manager"
            to="/article"
            @click.native="closeMobileMenu"
            class="text-2xl font-semibold tracking-tight hover:text-neutral-500 transition-colors"
          >后台</router-link>
          <a
            href="javascript:void(0)"
            class="text-2xl font-semibold tracking-tight hover:text-neutral-500 transition-colors"
            @click="doLogout"
          >退出</a>
        </template>
        <a
          v-else
          href="javascript:void(0)"
          class="text-2xl font-semibold tracking-tight hover:text-neutral-500 transition-colors"
          @click="goLogin"
        >登录</a>
        <div class="divider-line visible h-px bg-neutral-200 my-2" style="width:48px"></div>
        <div class="flex gap-4 pt-2">
          <a v-if="site.socials.github" :href="site.socials.github" target="_blank" rel="noopener noreferrer" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="GitHub"><icon-github :width="20" /></a>
          <a v-if="site.socials.bilibili" :href="site.socials.bilibili" target="_blank" rel="noopener noreferrer" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="Bilibili"><icon-bilibili :width="20" /></a>
          <a v-if="site.socials.email" :href="'mailto:' + site.socials.email" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="给我写信"><icon-mail :width="20" /></a>
        </div>
      </div>
    </div>

    <!-- ======== 搜索覆盖层 ======== -->
    <div
      id="search-overlay"
      class="search-overlay fixed inset-0 z-[70] bg-neutral-900/60 backdrop-blur-sm"
      @click="closeSearchOverlay($event)"
    >
      <div class="search-box max-w-2xl mx-auto mt-32 px-6">
        <div class="bg-white rounded-2xl shadow-2xl overflow-hidden">
          <div class="flex items-center px-5 h-14 border-b border-neutral-100">
            <icon-search :width="18" class="text-neutral-400 shrink-0" />
            <input
              ref="searchInput"
              v-model="searchQuery"
              type="text"
              placeholder="搜索文章…"
              class="flex-1 px-3 text-sm outline-none placeholder:text-neutral-400 bg-transparent"
              @input="handleSearch"
            >
            <kbd class="hidden sm:inline text-[10px] font-medium text-neutral-400 border border-neutral-200 rounded px-1.5 py-0.5">ESC</kbd>
          </div>
          <div class="max-h-72 overflow-y-auto">
            <div v-if="!searchQuery.trim()" class="px-5 py-8 text-center text-sm text-neutral-400">输入关键词开始搜索</div>
            <div v-else-if="searchResults.length === 0" class="px-5 py-8 text-center text-sm text-neutral-400">没有找到相关文章</div>
            <a
              v-for="item in searchResults"
              :key="item.id"
              href="javascript:void(0)"
              class="flex items-center justify-between px-5 py-3 hover:bg-neutral-50 transition-colors group"
              @click="goPost(item.id)"
            >
              <div>
                <div class="text-sm font-medium group-hover:text-neutral-600 transition-colors">{{ item.title }}</div>
                <div class="text-xs text-neutral-400 mt-0.5">{{ categoryName(item.categoryId) }} · {{ formatTime(item.createTime) }}</div>
              </div>
              <icon-arrow-right :width="14" class="text-neutral-300 group-hover:text-neutral-500 transition-colors shrink-0 ml-4" />
            </a>
          </div>
        </div>
      </div>
    </div>

    <main class="front-main">
      <router-view />
    </main>

    <!-- ======== 页脚 ======== -->
    <footer class="border-t border-neutral-100 px-6 py-12">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-2.5">
          <div class="w-6 h-6 bg-neutral-900 rounded-md flex items-center justify-center">
            <img src="/my_logo.svg" alt="logo" class="w-4 h-4 object-contain">
          </div>
          <span class="text-sm text-neutral-400">© {{ yearRange }} {{ site.author }} · {{ site.slogan }}</span>
        </div>
        <div class="flex items-center gap-6">
          <a v-if="site.socials.github" :href="site.socials.github" target="_blank" rel="noopener noreferrer" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="GitHub">
            <icon-github :width="14" />
          </a>
          <a v-if="site.socials.bilibili" :href="site.socials.bilibili" target="_blank" rel="noopener noreferrer" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="Bilibili">
            <icon-bilibili :width="14" />
          </a>
          <a v-if="site.socials.email" :href="'mailto:' + site.socials.email" class="text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="给我写信">
            <icon-mail :width="14" />
          </a>
          <a :href="feedUrl" target="_blank" rel="noopener noreferrer" class="text-xs text-neutral-400 hover:text-neutral-900 transition-colors" aria-label="RSS 订阅">RSS</a>
        </div>
      </div>
    </footer>

    <!-- ======== 回到顶部 ======== -->
    <button
      id="back-to-top"
      class="back-to-top fixed bottom-6 right-6 z-40 w-10 h-10 bg-neutral-900 text-white rounded-full flex items-center justify-center shadow-lg hover:bg-neutral-700 transition-colors duration-300"
      aria-label="回到顶部"
      @click="window.scrollTo({ top: 0, behavior: 'smooth' })"
    >
      <icon-arrow-up :width="16" />
    </button>
  </div>
</template>

<script>
import { SITE } from '@/config/site'
import { getStoredUser, isManager, logout } from '@/utils/auth'
import request, { API_BASE } from '@/utils/request'

export default {
  name: 'FrontLayout',
  data() {
    return {
      site: SITE,
      currentUser: getStoredUser(),
      categoryList: [],
      searchQuery: '',
      searchResults: [],
      searchTimer: null
    }
  },
  created() {
    // 搜索结果里展示分类名
    request.get('/category/selectAll').then(res => {
      if (res.code === '200') {
        this.categoryList = res.data || []
      }
    }).catch(() => {})
  },
  computed: {
    manager() {
      return isManager(this.currentUser)
    },
    feedUrl() {
      return API_BASE + '/feed.xml'
    },
    yearRange() {
      const now = new Date().getFullYear()
      return now > this.site.startYear
          ? `${this.site.startYear} - ${now}`
          : String(now)
    }
  },
  mounted() {
    this.onScroll = () => {
      const navbar = this.$refs.navbar
      if (window.scrollY > 20) {
        navbar.classList.add('nav-scrolled')
      } else {
        navbar.classList.remove('nav-scrolled')
      }
      const backToTop = this.$el.querySelector('#back-to-top')
      if (window.scrollY > 600) {
        backToTop.classList.add('show')
      } else {
        backToTop.classList.remove('show')
      }
    }
    window.addEventListener('scroll', this.onScroll, { passive: true })

    this.onKeydown = (e) => {
      if (e.key === 'Escape') {
        this.closeSearchPanel()
        const mm = this.$el.querySelector('#mobile-menu')
        if (mm.classList.contains('open')) {
          this.toggleMobileMenu()
        }
      }
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault()
        this.openSearch()
      }
    }
    document.addEventListener('keydown', this.onKeydown)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.onScroll)
    document.removeEventListener('keydown', this.onKeydown)
  },
  watch: {
    '$route.path'() {
      this.currentUser = getStoredUser()
    }
  },
  methods: {
    window,
    goLogin() {
      this.closeMobileMenu()
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
    },
    doLogout() {
      logout().then(() => {
        this.currentUser = null
        this.closeMobileMenu()
        this.$message.success('已退出登录')
        if (this.$route.meta.requiresAuth) {
          this.$router.push('/')
        }
      })
    },
    // === 移动端菜单 ===
    toggleMobileMenu() {
      const mm = this.$el.querySelector('#mobile-menu')
      mm.classList.toggle('open')
      document.body.style.overflow = mm.classList.contains('open') ? 'hidden' : ''
    },
    closeMobileMenu() {
      const mm = this.$el.querySelector('#mobile-menu')
      if (mm.classList.contains('open')) {
        mm.classList.remove('open')
        document.body.style.overflow = ''
      }
    },
    // === 搜索 ===
    openSearch() {
      const overlay = this.$el.querySelector('#search-overlay')
      overlay.classList.add('open')
      document.body.style.overflow = 'hidden'
      setTimeout(() => this.$refs.searchInput && this.$refs.searchInput.focus(), 100)
    },
    closeSearchOverlay(e) {
      if (e.target === e.currentTarget) {
        this.closeSearchPanel()
      }
    },
    closeSearchPanel() {
      const overlay = this.$el.querySelector('#search-overlay')
      if (!overlay.classList.contains('open')) {
        return
      }
      overlay.classList.remove('open')
      document.body.style.overflow = ''
      this.searchQuery = ''
      this.searchResults = []
    },
    handleSearch() {
      // 实时过滤：300ms 防抖走后端标题模糊查（游客恒为已发布）
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      this.searchTimer = setTimeout(() => {
        const query = this.searchQuery.trim()
        if (!query) {
          this.searchResults = []
          return
        }
        request.get('/article/selectPage', {
          params: { pageNum: 1, pageSize: 20, articleTitle: query, status: 'published' }
        }).then(res => {
          if (res.code === '200') {
            this.searchResults = (res.data && res.data.records) || []
          }
        }).catch(() => {})
      }, 300)
    },
    goPost(id) {
      this.closeSearchPanel()
      this.$router.push('/post/' + id)
    },
    categoryName(categoryId) {
      const category = this.categoryList.find(item => item.id === categoryId)
      return category ? category.name : '未分类'
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
  }
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.front-main {
  flex: 1;
  padding-top: 64px; /* 固定导航栏高度 */
}

/* 导航栏滚动效果 */
.nav-scrolled {
  background: rgba(255, 255, 255, 0.92) !important;
  backdrop-filter: blur(16px) !important;
  border-bottom: 1px solid #e5e5e5 !important;
}

/* 移动菜单 */
.mobile-menu {
  transform: translateX(100%);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.mobile-menu.open {
  transform: translateX(0);
}

/* 搜索框 */
.search-overlay {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}
.search-overlay.open {
  opacity: 1;
  pointer-events: auto;
}
.search-overlay .search-box {
  transform: translateY(-20px) scale(0.98);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.search-overlay.open .search-box {
  transform: translateY(0) scale(1);
}

/* 回到顶部 */
.back-to-top {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease, transform 0.3s ease;
  transform: translateY(8px);
}
.back-to-top.show {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}
</style>
