import Vue from 'vue'
import request from '@/utils/request'
import { SITE } from '@/config/site'

/**
 * 站点信息运行时状态。
 *
 * 数据来源两层：
 * 1) `config/site.js` 的 SITE —— 代码默认值（永远有一份兜底，后台配置损坏也不白屏）；
 * 2) 后端 `site_config` 表（GET /site）—— 按 key 覆盖默认值，后台「站点信息」页可编辑。
 *
 * 合并规则：对象（socials / profile）做一层深合并，数组（aboutLines / bio / ...）**整段替换**
 * （后端传空数组就代表「这个版块不显示」，不能被默认值又填回来）。
 */
const DEFAULTS = JSON.parse(JSON.stringify(SITE))

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function isPlainObject(value) {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

export const siteState = Vue.observable(clone(DEFAULTS))

/** 是否已应用后台配置（后台页用来提示「当前是后台配置 / 代码默认」） */
siteState.configured = false

export function applyConfig(config) {
  const source = config || {}
  const next = clone(DEFAULTS)
  Object.keys(source).forEach(key => {
    // 后端白名单已挡过一遍，这里再挡一次，防止脏数据污染默认结构
    if (!Object.prototype.hasOwnProperty.call(next, key)) {
      return
    }
    const value = source[key]
    if (isPlainObject(next[key]) && isPlainObject(value)) {
      next[key] = Object.assign({}, next[key], value)
    } else {
      next[key] = value
    }
  })
  Object.keys(next).forEach(key => {
    siteState[key] = next[key]
  })
  return next
}

export function loadSite() {
  return request.get('/site').then(res => {
    if (res.code === '200') {
      const data = res.data || {}
      siteState.configured = Object.keys(data).length > 0
      applyConfig(data)
    }
  }).catch(() => {
    // 后端没起或接口异常时静默回退代码默认值，不打扰访客
  })
}

export function saveSiteConfig(config) {
  return request.put('/site', config).then(res => {
    if (res.code === '200') {
      const data = res.data || {}
      siteState.configured = Object.keys(data).length > 0
      applyConfig(data)
    }
    return res
  })
}

/** 恢复代码默认值（后台保存空对象即可） */
export function resetSiteConfig() {
  return saveSiteConfig({})
}
