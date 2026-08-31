<template>
  <el-container class="admin-layout">
    <el-header class="admin-header">
      <div class="brand" @click="$router.push('/')">
        <img src="@/assets/my_logo.svg" alt="logo">
        <span>{{ site.name }} · 管理后台</span>
      </div>

      <div class="account-area">
        <template v-if="currentUser">
          <span class="account-name">
            {{ currentUser.nickname || currentUser.username }}
          </span>
          <el-tag size="mini" effect="dark">{{ role }}</el-tag>
          <el-button type="text" @click="doLogout">退出</el-button>
        </template>
      </div>
    </el-header>

    <el-container>
      <el-aside class="admin-aside">
        <el-menu
            :default-active="menuActive"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
        >
          <el-menu-item index="/article">
            <i class="el-icon-document"></i><span>文章管理</span>
          </el-menu-item>

          <el-menu-item index="/category">
            <i class="el-icon-folder"></i><span>分类管理</span>
          </el-menu-item>

          <el-menu-item v-if="superAdmin" index="/user">
            <i class="el-icon-user"></i><span>账号管理</span>
          </el-menu-item>

          <el-menu-item index="/">
            <i class="el-icon-monitor"></i><span>查看前台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { SITE } from '@/config/site'
import { getStoredUser, isSuperAdmin, logout, roleText } from '@/utils/auth'

export default {
  name: 'AdminLayout',
  data() {
    return {
      site: SITE,
      currentUser: getStoredUser()
    }
  },
  computed: {
    superAdmin() {
      return isSuperAdmin(this.currentUser)
    },
    role() {
      return roleText(this.currentUser)
    },
    menuActive() {
      // 编辑器页高亮"文章管理"
      if (this.$route.path.startsWith('/article')) {
        return '/article'
      }
      return this.$route.path
    }
  },
  watch: {
    '$route.path'() {
      this.currentUser = getStoredUser()
    }
  },
  methods: {
    doLogout() {
      logout().then(() => {
        this.currentUser = null
        this.$message.success('已退出登录')
        this.$router.push('/')
      })
    }
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-header {
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
  font-size: 20px;
}

.brand img {
  width: 36px;
  margin-right: 16px;
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

.admin-aside {
  background-color: #545c64;
  width: 220px !important;
  min-height: calc(100vh - 60px);
  overflow: hidden;
}

.admin-aside .el-menu {
  border-right: none;
}
</style>
