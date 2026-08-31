<template>
  <component :is="layoutComponent" />
</template>

<script>
import FrontLayout from '@/layouts/FrontLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

/**
 * 顶层只做布局切换：
 * - 路由 meta.layout === 'admin' → 后台布局（侧边栏）
 * - 其余 → 前台布局（顶部导航 + 页脚）
 */
export default {
  name: 'App',
  computed: {
    layoutComponent() {
      return this.$route.meta.layout === 'admin' ? AdminLayout : FrontLayout
    }
  }
}
</script>
