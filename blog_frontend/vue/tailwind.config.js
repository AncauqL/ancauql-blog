module.exports = {
  content: [
    './public/index.html',
    './src/**/*.{vue,js}',
  ],
  // preflight 关闭：避免 reset 样式打爆 Element UI；
  // 副作用是 border-* 工具类缺 border-style，在 global.css 手工补齐
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
