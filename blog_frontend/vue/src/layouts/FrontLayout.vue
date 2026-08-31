<template>
  <div class="front-layout">
    <header class="front-header">
      <div class="front-header-inner">
        <div class="brand" @click="$router.push('/')">
          <img src="@/assets/my_logo.svg" alt="logo">
          <span class="brand-name">{{ site.name }}</span>
        </div>

        <nav class="front-nav">
          <router-link to="/" exact class="nav-link">首页</router-link>
          <router-link to="/archive" class="nav-link">归档</router-link>
          <router-link to="/aboutme" class="nav-link">关于我</router-link>
        </nav>

        <div class="account-area">
          <template v-if="currentUser">
            <span class="account-name">
              {{ currentUser.nickname || currentUser.username }}
            </span>
            <el-button
                v-if="manager"
                type="text"
                class="account-link"
                @click="$router.push('/article')"
            >
              进入后台
            </el-button>
            <el-button
                type="text"
                class="account-link"
                @click="doLogout"
            >
              退出
            </el-button>
          </template>
          <el-button
              v-else
              type="text"
              class="account-link"
              @click="goLogin"
          >
            登录
          </el-button>
        </div>
      </div>
    </header>

    <main class="front-main">
      <router-view />
    </main>

    <footer class="front-footer">
      <p>© {{ yearRange }} {{ site.author }} · {{ site.slogan }}</p>
      <p v-if="site.icp">{{ site.icp }}</p>
    </footer>
  </div>
</template>

<script>
import { SITE } from '@/config/site'
import { getStoredUser, isManager, logout } from '@/utils/auth'

export default {
  name: 'FrontLayout',
  data() {
    return {
      site: SITE,
      currentUser: getStoredUser()
    }
  },
  computed: {
    manager() {
      return isManager(this.currentUser)
    },
    yearRange() {
      const now = new Date().getFullYear()
      return now > this.site.startYear
          ? `${this.site.startYear} - ${now}`
          : String(now)
    }
  },
  watch: {
    '$route.path'() {
      this.currentUser = getStoredUser()
    }
  },
  methods: {
    goLogin() {
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
    },
    doLogout() {
      logout().then(() => {
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

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.front-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(6px);
  border-bottom: 1px solid #ebeef5;
}

.front-header-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand img {
  width: 30px;
}

.brand-name {
  font-size: 19px;
  font-weight: 700;
  color: #1f1e33;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", serif;
  white-space: nowrap;
}

.front-nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  font-size: 15px;
  color: #606266;
  text-decoration: none;
  padding: 4px 2px;
  border-bottom: 2px solid transparent;
  transition: color 0.2s ease;
  white-space: nowrap;
}

.nav-link:hover {
  color: #1f1e33;
}

.nav-link.router-link-active {
  color: #1f1e33;
  font-weight: 600;
  border-bottom-color: #1f1e33;
}

.account-area {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}

.account-name {
  font-size: 14px;
  color: #606266;
  margin-right: 4px;
  white-space: nowrap;
}

.account-link {
  padding: 0 6px;
  color: #606266;
}

.account-link:hover {
  color: #1f1e33;
}

.front-main {
  flex: 1;
}

.front-footer {
  padding: 28px 20px 32px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.front-footer p {
  margin: 4px 0;
}

@media (max-width: 640px) {
  .front-header-inner {
    gap: 14px;
    padding: 0 12px;
  }

  .brand-name {
    display: none;
  }

  .front-nav {
    gap: 14px;
  }

  .account-name {
    display: none;
  }
}
</style>
