<template>
  <el-container>
    <el-header style="background-color: #1f1e33;height: 60px">
      <img src="@/assets/my_logo.svg" style="width: 40px;position: relative;top: 12px">
      <span style="font-size: 24px;margin-left: 24px;color: white">AncauqL Blog</span>
    </el-header>

    <el-container>
      <el-aside style="background-color: #545c64;min-height: 100vh;width: 250px;overflow: hidden;">
        <el-menu :default-active="$route.path" router background-color="#545c64" text-color="#fff" active-text-color="#ffd04b">
          <el-menu-item index="/">
            <template slot="title">
              <i class="el-icon-location"></i><span>系统首页</span>
            </template>
          </el-menu-item>
          <el-submenu index="2">
            <template slot="title">
              <i class="el-icon-location"></i><span>用户管理</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="/about">管理员信息</el-menu-item>
<!--              <el-menu-item index="/user">用户信息</el-menu-item>-->
              <el-menu-item index="/category">分类管理</el-menu-item>
              <el-menu-item index="/article">文章管理</el-menu-item>
            </el-menu-item-group>
          </el-submenu>
          <el-submenu index="3">
            <template slot="title">
              <i class="el-icon-location"></i><span>信息管理</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="3-1">公告信息</el-menu-item>
            </el-menu-item-group>
          </el-submenu>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'App',
}
</script>

<style>
.el-menu {
  border-right: none !important;
}
</style>
