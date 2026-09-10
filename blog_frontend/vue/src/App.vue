<template>
  <component :is="layoutComponent" />
</template>

<script>
import FrontLayout from '@/layouts/FrontLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { loadSite } from '@/store/site'

/**
 * 顶层只做布局切换：
 * - 路由 meta.layout === 'admin' → 后台布局（侧边栏）
 * - 其余 → 前台布局（顶部导航 + 页脚）
 *
 * 启动时拉一次后台站点配置（公开接口），失败就沿用 config/site.js 的默认值。
 */
export default {
  name: 'App',
  created() {
    loadSite()
  },
  computed: {
    layoutComponent() {
      return this.$route.meta.layout === 'admin' ? AdminLayout : FrontLayout
    }
  }
}
</script>
