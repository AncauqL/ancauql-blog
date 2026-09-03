/**
 * 站点信息配置（唯一数据源）。
 * 首页简介与 AboutMe 详情页都从这里读，改这一处即可同步。
 * 目前写死在前端；后续若做站点配置表可整体迁移到后台可编辑。
 */
export const SITE = {
  name: 'AncauqL',
  author: 'AncauqL',
  slogan: '思考，记录，然后遗忘。',
  // 首页 Hero 描述
  heroText: '这里是我存放想法的地方。关于设计、技术、生活，以及一切值得被文字捕捉的瞬间。',

  // 身份一行（学校 / 专业 / 年级）
  identity: '大连理工大学 · 计算机科学与技术 · 2024 级',

  // 首页「关于我」区的简短简介（v-for 渲染）
  aboutLines: [
    '你好，我是 AncauqL，一个对设计和代码都有执念的人。',
    '这个博客是我思考的副产品——记录学习、记录生活、记录那些值得留下的瞬间。',
    '少一分浮躁，多一分沉淀。'
  ],

  /**
   * AboutMe（/aboutme）详情页数据。
   * 除 name / identity / motto 外，数组留空则对应版块**自动隐藏**；
   * 想展示时往里面填即可，文案日后慢慢补。
   */
  profile: {
    name: 'AncauqL',
    identity: '大连理工大学 · 计算机科学与技术 · 2024 级',
    motto: '少一分浮躁，多一分沉淀。',
    // 自我介绍正文（段落级）
    bio: [
      '这是关于我的一页，目前还在整理。先把结构搭好，具体文字我之后慢慢补。'
    ],
    // 技术栈：[{ label, desc }]
    skills: [],
    // 兴趣爱好：[string]
    interests: [],
    // 喜欢的作品：[string]（书 / 动漫 / 游戏 / 电影 / 音乐均可）
    favorites: [],
    // 经历 / 时间线：[{ period, text }]
    journey: []
  },

  // 肖像图（相对路径走 resolveAsset；空则用站名首字母占位，不会裂图）
  portrait: '',

  // 社交 / 联系方式（留空自动隐藏对应图标）
  socials: {
    github: 'https://github.com/AncauqL',
    bilibili: 'https://space.bilibili.com/470073596',
    email: 'ancauql@163.com',
    qq: '846094212'
  },

  // 备案号留空则页脚不显示
  icp: '',
  // 建站年份：用于首页“N 年写作”统计
  startYear: 2026
}
