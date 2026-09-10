<template>
  <component :is="layoutComponent" />
</template>

<script>
import FrontLayout from '@/layouts/FrontLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import request from '@/utils/request'
import { loadSite } from '@/store/site'

/**
 * 顶层只做布局切换：
 * - 路由 meta.layout === 'admin' → 后台布局（侧边栏）
 * - 其余 → 前台布局（顶部导航 + 页脚）
 *
 * 另外在这里做两件全局的事：
 * 1) 启动时拉一次后台站点配置（公开接口），失败就用 config/site.js 的默认值；
 * 2) 每次路由切换上报一次页面访问（POST /visit），管理员自己的浏览由后端忽略。
 */
export default {
  name: 'App',
  data() {
    return {
      lastTrackedPath: ''
    }
  },
  created() {
    loadSite()
  },
  mounted() {
    this.trackVisit(this.$route)
  },
  watch: {
    $route(to) {
      this.trackVisit(to)
    }
  },
  computed: {
    layoutComponent() {
      return this.$route.meta.layout === 'admin' ? AdminLayout : FrontLayout
    }
  },
  methods: {
    trackVisit(route) {
      if (!route || !route.path) {
        return
      }
      const fullPath = route.fullPath || route.path
      if (fullPath === this.lastTrackedPath) {
        return
      }
      this.lastTrackedPath = fullPath
      const rawId = route.name === 'ArticleDetail' ? Number(route.params.id) : null
      // 失败也不打扰用户：统计不是关键路径
      request.post('/visit', {
        path: fullPath.slice(0, 200),
        articleId: Number.isInteger(rawId) ? rawId : null
      }).catch(() => {})
    }
  }
}
</script>
