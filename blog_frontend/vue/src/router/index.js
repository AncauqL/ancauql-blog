import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('../views/Category.vue'),
    meta: {
      requiresAuth: true,
      roles: ['SUPER_ADMIN', 'ADMIN'],
      layout: 'admin'
    }
  },
  {
    path: '/article',
    name: 'Article',
    component: () => import('../views/Article.vue'),
    meta: {
      requiresAuth: true,
      roles: ['SUPER_ADMIN', 'ADMIN'],
      layout: 'admin'
    }
  },
  {
    path: '/article/edit/:id?',
    name: 'ArticleEditor',
    component: () => import('../views/ArticleEditor.vue'),
    meta: {
      requiresAuth: true,
      roles: ['SUPER_ADMIN', 'ADMIN'],
      layout: 'admin'
    }
  },
  {
    path: '/archive',
    name: 'Archive',
    component: () => import('../views/Archive.vue')
  },
  {
    path: '/post/:id',
    name: 'ArticleDetail',
    component: () => import('../views/ArticleDetail.vue')
  },
  {
    path: '/aboutme',
    name: 'aboutme',
    component: () => import('../views/AboutMe.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('../views/User.vue'),
    meta: {
      requiresAuth: true,
      roles: ['SUPER_ADMIN'],
      layout: 'admin'
    }
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('blog_token')
  const user = getStoredUser()

  if (token && !user) {
    localStorage.removeItem('blog_token')
    localStorage.removeItem('blog_user')
  }

  if (to.path === '/login' && token && user) {
    next('/article')
    return
  }

  if (to.meta.requiresAuth && (!token || !user)) {
    next({
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    })
    return
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!user || !to.meta.roles.includes(user.role)) {
      next('/')
      return
    }
  }

  next()
})

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('blog_user') || 'null')
  } catch (e) {
    return null
  }
}

export default router
