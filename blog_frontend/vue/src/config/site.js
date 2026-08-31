/**
 * 站点信息配置。
 * 目前写死在前端；等做站点配置表（M4）后改为后台可编辑。
 */
export const SITE = {
  name: '墨白',
  author: 'AncauqL',
  slogan: '思考，记录，然后遗忘。',
  // 首页 Hero 描述
  heroText: '这里是我存放想法的地方。关于设计、技术、生活，以及一切值得被文字捕捉的瞬间。',
  // 关于我（首页三段文字）
  aboutP1: '你好，我是AncauqL。一个对设计和代码都有执念的人。',
  aboutP2: '这个博客是我思考的副产品——记录学习、记录生活、记录那些值得留下的瞬间。',
  aboutP3: '我相信好的设计是隐形的，好的文字是克制的，好的技术是谦逊的。',
  // 肖像图（相对路径走 resolveAsset；空则显示灰底占位）
  portrait: '',
  // 社交链接（空则不显示图标）
  github: '',
  twitter: '',   // Twitter/X 链接，空则隐藏
  dribbble: '',  // Dribbble 链接，空则隐藏
  email: '',
  // 备案号留空则页脚不显示
  icp: '',
  // 建站年份：用于首页“N 年写作”统计
  startYear: 2026
}
