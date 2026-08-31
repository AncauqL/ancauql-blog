import Vue from 'vue'
import IconBase from './IconBase'

/**
 * 静态 lucide 图标集合（SVG path 抄自 lucide v0.x 官方源码）。
 * 不引 iconify 运行时（其默认从 api.iconify.design 拉数据，国内不稳）。
 * 用法：<icon-search :width="18" />
 */

const define = (name, paths) => {
  Vue.component(name, {
    name,
    functional: true,
    props: { width: { type: [Number, String], default: 18 } },
    render(h, ctx) {
      const { width } = ctx.props
      return h(IconBase, {
        props: { width, paths },
        class: ctx.data.class,
        staticClass: ctx.data.staticClass,
        style: ctx.data.style
      })
    }
  })
}

define('icon-search', ['circle cx="11" cy="11" r="8"', 'path d="m21 21-4.3-4.3"'])
define('icon-menu', ['line x1="4" x2="20" y1="6" y2="6"', 'line x1="4" x2="20" y1="12" y2="12"', 'line x1="4" x2="20" y1="18" y2="18"'])
define('icon-x', ['path d="M18 6 6 18"', 'path d="m6 6 12 12"'])
define('icon-arrow-down', ['path d="M12 5v14"', 'path d="m19 12-7 7-7-7"'])
define('icon-arrow-up', ['path d="m5 12 7-7 7 7"', 'path d="M12 19V5"'])
define('icon-arrow-right', ['path d="M5 12h14"', 'path d="m12 5 7 7-7 7"'])
define('icon-chevron-down', ['path d="m6 9 6 6 6-6"'])
define('icon-mail', ['rect width="20" height="16" x="2" y="4" rx="2"', 'path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"'])
define('icon-github', ['path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"', 'path d="M9 18c-4.51 2-5-2-7-2"'])
define('icon-twitter', ['path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z'])
define('icon-dribbble', ['circle cx="12" cy="12" r="10"', 'path d="M19.13 5.09C15.22 9.14 10 10.44 2.25 10.94"', 'path d="M21.75 12.84c-6.62-1.41-12.14 1-16.38 6.32"', 'path d="M8.56 2.75c4.37 6 6 9.42 8 17.72"'])
