import request from '@/utils/request'

/**
 * 登录态工具：前台 / 后台两套布局共用，避免逻辑漂移。
 */

export function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('blog_user') || 'null')
  } catch (e) {
    return null
  }
}

export function isManager(user) {
  const u = user || getStoredUser()
  return !!u && ['SUPER_ADMIN', 'ADMIN'].includes(u.role)
}

export function isSuperAdmin(user) {
  const u = user || getStoredUser()
  return !!u && u.role === 'SUPER_ADMIN'
}

export function clearAuth() {
  localStorage.removeItem('blog_token')
  localStorage.removeItem('blog_user')
}

/** 通知后端退出并清理本地登录态；无论后端是否成功都会清理。 */
export function logout() {
  return request.post('/auth/logout').catch(() => {}).finally(() => {
    clearAuth()
  })
}

export function roleText(user) {
  const u = user || getStoredUser()
  if (!u) {
    return ''
  }
  if (u.role === 'SUPER_ADMIN') {
    return '超级管理员'
  }
  if (u.role === 'ADMIN') {
    return '管理员'
  }
  return u.role
}
