<template>
  <el-container>
    <el-header class="app-header">
      <div class="brand" @click="$router.push('/')">
        <img src="@/assets/my_logo.svg">
        <span>AncauqL Blog</span>
      </div>

      <div class="account-area">
        <template v-if="currentUser">
          <span class="account-name">
            {{ currentUser.nickname || currentUser.username }}
          </span>
          <el-tag size="mini" effect="dark">{{ roleText }}</el-tag>
          <el-button type="text" @click="logout">退出</el-button>
        </template>

        <el-button
            v-else
            type="text"
            @click="goLogin"
        >
          登录
        </el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside style="background-color: #545c64;min-height: 100vh;width: 250px;overflow: hidden;">
        <el-menu :default-active="$route.path" router background-color="#545c64" text-color="#fff" active-text-color="#ffd04b">

          <el-menu-item index="/">
            <template slot="title">
              <i class="el-icon-location"></i><span>系统首页</span>
            </template>
          </el-menu-item>

          <el-menu-item v-if="isManager" index="/category">
            <template slot="title">
              <i class="el-icon-folder"></i><span>分类管理</span>
            </template>
          </el-menu-item>

          <el-menu-item v-if="isManager" index="/article">
            <template slot="title">
              <i class="el-icon-document"></i><span>文章管理</span>
            </template>
          </el-menu-item>

          <el-menu-item v-if="isSuperAdmin" index="/user">
            <template slot="title">
              <i class="el-icon-user"></i><span>账号管理</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/aboutme">
            <template slot="title">
              <i class="el-icon-info"></i><span>关于我</span>
            </template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'App',
  data() {
    return {
      currentUser: null
    }
  },
  computed: {
    isManager() {
      return this.currentUser &&
          ['SUPER_ADMIN', 'ADMIN'].includes(this.currentUser.role)
    },
    isSuperAdmin() {
      return this.currentUser &&
          this.currentUser.role === 'SUPER_ADMIN'
    },
    roleText() {
      if (!this.currentUser) {
        return ''
      }
      if (this.currentUser.role === 'SUPER_ADMIN') {
        return '超级管理员'
      }
      if (this.currentUser.role === 'ADMIN') {
        return '管理员'
      }
      return this.currentUser.role
    }
  },
  watch: {
    '$route.path'() {
      this.loadStoredUser()
    }
  },
  created() {
    this.loadStoredUser()
  },
  methods: {
    loadStoredUser() {
      try {
        this.currentUser = JSON.parse(localStorage.getItem('blog_user') ||
            'null')
      } catch (e) {
        this.currentUser = null
      }
    },
    goLogin() {
      this.$router.push({
        path: '/login',
        query: {
          redirect: this.$route.fullPath
        }
      })
    },
    logout() {
      request.post('/auth/logout').finally(() => {
        localStorage.removeItem('blog_token')
        localStorage.removeItem('blog_user')
        this.currentUser = null
        this.$message.success('已退出登录')
        if (this.$route.meta.requiresAuth) {
          this.$router.push('/')
        }
      })
    }
  }
}
</script>

<style>
.el-menu {
  border-right: none !important;
}

.app-header {
  height: 60px;
  background-color: #1f1e33;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: white;
  font-size: 24px;
}

.brand img {
  width: 40px;
  margin-right: 24px;
}

.account-area {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
}

.account-name {
  font-size: 14px;
}

.account-area .el-button {
  color: #fff;
}
</style>
