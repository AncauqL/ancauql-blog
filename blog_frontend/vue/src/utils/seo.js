import { siteState } from '@/store/site'
import { API_BASE, resolveAsset } from '@/utils/request'

/**
 * 极简 SEO 工具：SPA 没有服务端渲染，只能在前端路由切换后改 document 里的
 * title / description / Open Graph。对浏览器标签页、以及会执行 JS 的分享抓取有效；
 * 对不执行 JS 的爬虫，兜底靠 public/index.html 里的静态 meta 与后端 sitemap.xml。
 */

function upsertMeta(attr, key, content) {
  if (content === undefined || content === null) {
    return
  }
  let el = document.head.querySelector(`meta[${attr}="${key}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function upsertCanonical(url) {
  let el = document.head.querySelector('link[rel="canonical"]')
  if (!el) {
    el = document.createElement('link')
    el.setAttribute('rel', 'canonical')
    document.head.appendChild(el)
  }
  el.setAttribute('href', url)
}

/** 把站内相对图片地址转成绝对地址（社交卡片要求绝对 URL） */
function absoluteImage(path) {
  if (!path) {
    return ''
  }
  if (/^https?:\/\//i.test(path)) {
    return path
  }
  return API_BASE + resolveAsset(path)
}

/**
 * @param {object} options
 * @param {string} [options.title]       页面标题（会拼成「标题 · 站名」）
 * @param {string} [options.description] 页面描述
 * @param {string} [options.image]       分享卡片图片（相对路径或绝对 URL）
 * @param {string} [options.type]        og:type，默认 website，文章页传 article
 * @param {string} [options.path]        canonical 路径，默认用当前地址
 * @param {boolean} [options.noindex]    是否标记 noindex（搜索页 / 404）
 */
export function setSeo(options = {}) {
  const siteName = siteState.name || '博客'
  const { title, description, image, type = 'website', path, noindex } = options

  document.title = title ? `${title} · ${siteName}` : siteName

  const desc = description || siteState.slogan || siteState.heroText || ''
  upsertMeta('name', 'description', desc)
  upsertMeta('property', 'og:title', title || siteName)
  upsertMeta('property', 'og:description', desc)
  upsertMeta('property', 'og:type', type)
  upsertMeta('property', 'og:site_name', siteName)
  upsertMeta('property', 'og:url', location.href)
  upsertMeta('name', 'twitter:title', title || siteName)
  upsertMeta('name', 'twitter:description', desc)

  const cover = absoluteImage(image)
  if (cover) {
    upsertMeta('property', 'og:image', cover)
    upsertMeta('name', 'twitter:image', cover)
  }

  upsertMeta('name', 'robots', noindex ? 'noindex,follow' : 'index,follow')
  upsertCanonical(location.origin + (path || location.pathname))
}
