/**
 * lucide 图标的静态 SVG 底座：零运行时依赖（不用 iconify CDN）。
 * 用法：<icon-search :width="18" />，尺寸/颜色跟随 currentColor。
 *
 * filled=true 时按“实心品牌图标”渲染（fill=currentColor、无描边），
 * 用于 Bilibili 这类官方实心 Logo；默认仍为 lucide 描边风格。
 */
export default {
  functional: true,
  name: 'IconBase',
  props: {
    width: { type: [Number, String], default: 18 },
    paths: { type: Array, required: true },
    filled: { type: Boolean, default: false }
  },
  render(h, ctx) {
    const { width, paths, filled } = ctx.props
    return h(
      'svg',
      {
        attrs: {
          xmlns: 'http://www.w3.org/2000/svg',
          width,
          height: width,
          viewBox: '0 0 24 24',
          fill: filled ? 'currentColor' : 'none',
          stroke: filled ? 'none' : 'currentColor',
          'stroke-width': filled ? 0 : 2,
          'stroke-linecap': 'round',
          'stroke-linejoin': 'round',
          'aria-hidden': 'true'
        },
        class: ctx.data.class,
        staticClass: ctx.data.staticClass,
        style: ctx.data.style
      },
      paths.map((d, i) => h('path', { key: i, attrs: { d } }))
    )
  }
}
