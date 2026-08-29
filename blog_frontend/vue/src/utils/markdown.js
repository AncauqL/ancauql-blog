import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js/lib/core'
import { API_BASE } from '@/utils/request'

import bash from 'highlight.js/lib/languages/bash'
import shell from 'highlight.js/lib/languages/shell'
import powershell from 'highlight.js/lib/languages/powershell'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import sql from 'highlight.js/lib/languages/sql'
import xml from 'highlight.js/lib/languages/xml'
import json from 'highlight.js/lib/languages/json'
import yaml from 'highlight.js/lib/languages/yaml'
import ini from 'highlight.js/lib/languages/ini'
import properties from 'highlight.js/lib/languages/properties'
import dockerfile from 'highlight.js/lib/languages/dockerfile'
import nginx from 'highlight.js/lib/languages/nginx'
import plaintext from 'highlight.js/lib/languages/plaintext'

import 'highlight.js/styles/github.css'
import '@/assets/css/markdown.css'

// 只注册博客会用到的语言，控制打包体积
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sh', bash)
hljs.registerLanguage('shell', shell)
hljs.registerLanguage('powershell', powershell)
hljs.registerLanguage('java', java)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('vue', xml)
hljs.registerLanguage('json', json)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('yml', yaml)
hljs.registerLanguage('ini', ini)
hljs.registerLanguage('toml', ini)
hljs.registerLanguage('conf', ini)
hljs.registerLanguage('properties', properties)
hljs.registerLanguage('dockerfile', dockerfile)
hljs.registerLanguage('nginx', nginx)
hljs.registerLanguage('plaintext', plaintext)
hljs.registerLanguage('text', plaintext)

const md = new MarkdownIt({
  html: true,      // 允许正文里混写 HTML，渲染后统一交给 DOMPurify 消毒
  linkify: true,   // 自动识别裸链接
  breaks: false
})

// 代码块：带语言标签 + 复制按钮的卡片结构
md.renderer.rules.fence = (tokens, idx) => {
  const token = tokens[idx]
  const info = token.info ? token.info.trim().split(/\s+/)[0] : ''
  const langLabel = info || 'text'

  let highlighted
  if (info && hljs.getLanguage(info)) {
    highlighted = hljs.highlight(token.content, {
      language: info,
      ignoreIllegals: true
    }).value
  } else {
    highlighted = md.utils.escapeHtml(token.content)
  }

  return (
    '<div class="code-block">' +
    '<div class="code-block-bar">' +
    '<span class="code-lang">' + md.utils.escapeHtml(langLabel) + '</span>' +
    '<button type="button" class="code-copy-btn">复制</button>' +
    '</div>' +
    '<pre class="hljs"><code>' + highlighted + '</code></pre>' +
    '</div>'
  )
}

// 外部链接新开标签页，站内链接保持默认行为
const defaultLinkOpen = md.renderer.rules.link_open ||
  ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  const href = tokens[idx].attrGet('href') || ''
  if (/^https?:\/\//i.test(href)) {
    tokens[idx].attrSet('target', '_blank')
    tokens[idx].attrSet('rel', 'noopener noreferrer')
  }
  return defaultLinkOpen(tokens, idx, options, env, self)
}

// 站内上传的图片使用相对路径存储（/uploads/...），渲染时补全后端地址
const defaultImage = md.renderer.rules.image

md.renderer.rules.image = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const src = token.attrGet('src') || ''
  if (src.startsWith('/uploads/')) {
    token.attrSet('src', API_BASE + src)
  }
  return defaultImage(tokens, idx, options, env, self)
}

/**
 * 把 Markdown 渲染成消毒后的 HTML。
 */
export function renderMarkdown(source) {
  const html = md.render(source || '')
  return DOMPurify.sanitize(html, {
    ADD_ATTR: ['target', 'rel']
  })
}

/**
 * 去掉 Markdown 语法，得到适合做摘要预览 / 字数统计的纯文本。
 */
export function stripMarkdown(source) {
  if (!source) {
    return ''
  }
  return String(source)
    // 整块移除围栏代码
    .replace(/```[\s\S]*?```/g, ' ')
    // 行内代码保留内容
    .replace(/`([^`]*)`/g, '$1')
    // 图片整体移除，链接保留文字
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    // HTML 标签
    .replace(/<[^>]+>/g, ' ')
    // 标题、引用、列表、任务列表前缀
    .replace(/^#{1,6}\s+/gm, '')
    .replace(/^>\s?/gm, '')
    .replace(/^\s*[-+*]\s+(\[[ xX]\]\s+)?/gm, '')
    .replace(/^\s*\d+\.\s+/gm, '')
    // 表格分隔线与竖线
    .replace(/^\s*\|?[\s:-]+\|[\s|:-]*$/gm, ' ')
    .replace(/\|/g, ' ')
    // 强调符号与水平线
    .replace(/(\*\*|__|\*|_|~~)/g, '')
    .replace(/^\s*(-{3,}|\*{3,})\s*$/gm, ' ')
    // 收敛空白
    .replace(/\s+/g, ' ')
    .trim()
}

/**
 * 统计字数：中日韩字符按字计，其余按空白分词计。
 */
export function countWords(source) {
  const text = stripMarkdown(source)
  if (!text) {
    return 0
  }
  const cjk = (text.match(/[一-鿿぀-ヿ가-힯]/g) || []).length
  const nonCjkText = text.replace(/[一-鿿぀-ヿ가-힯]/g, ' ')
  const words = nonCjkText.split(/\s+/).filter(Boolean).length
  return cjk + words
}

/**
 * 估算阅读时长（分钟），按每分钟 400 字/词。
 */
export function readingMinutes(source) {
  const count = countWords(source)
  if (count === 0) {
    return 0
  }
  return Math.max(1, Math.round(count / 400))
}
