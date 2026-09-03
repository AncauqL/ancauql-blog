<template>
  <div class="notfound">
    <div class="notfound-inner">
      <p class="code">404</p>
      <h1>页面不存在</h1>
      <p class="desc">你访问的地址可能已失效，或者输入有误。</p>
      <router-link to="/" class="home-link">
        返回首页
        <icon-arrow-right :width="15" />
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>

<style scoped>
.notfound {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.notfound-inner {
  max-width: 460px;
}

.code {
  font-size: 96px;
  line-height: 1;
  font-weight: 700;
  color: #e5e5e5;
  letter-spacing: 2px;
}

.notfound h1 {
  margin: 8px 0 0;
  font-size: 28px;
  font-weight: 600;
  color: #171717;
}

.desc {
  margin: 12px 0 28px;
  font-size: 15px;
  color: #737373;
  font-weight: 300;
}

.home-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 44px;
  padding: 0 24px;
  border-radius: 999px;
  background: #171717;
  color: #fff;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.2s ease;
}

.home-link:hover {
  background: #404040;
}
</style>
