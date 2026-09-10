<template>
  <div class="site-page">
    <div class="page-header">
      <div>
        <h2>站点信息</h2>
        <p>
          这里保存后会覆盖前端 <code>src/config/site.js</code> 里的同名配置；
          <el-tag :type="configured ? 'success' : 'info'" size="mini">
            {{ configured ? '当前使用后台配置' : '当前使用代码默认值' }}
          </el-tag>
        </p>
      </div>
      <div>
        <el-button @click="openFront">查看前台</el-button>
        <el-button type="warning" plain @click="resetAll">恢复代码默认</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </div>
    </div>

    <el-alert
        v-if="loadedFromServer"
        type="info"
        :closable="false"
        show-icon
        title="已读取后台配置"
        description="留空某项即表示该项按代码默认值显示（数组留空则该版块在前台隐藏）。"
        class="mb"
    />

    <el-form :model="form" label-width="130px" class="site-form">
      <div class="group">
        <h3>基本信息</h3>
        <el-form-item label="站点名称">
          <el-input v-model="form.name" placeholder="浏览器标题与页脚署名" />
        </el-form-item>
        <el-form-item label="作者署名">
          <el-input v-model="form.author" />
        </el-form-item>
        <el-form-item label="Slogan">
          <el-input v-model="form.slogan" />
        </el-form-item>
        <el-form-item label="身份一行">
          <el-input v-model="form.identity" placeholder="例如：大连理工大学 · 计算机科学与技术 · 2024 级" />
        </el-form-item>
        <el-form-item label="建站年份">
          <el-input-number v-model="form.startYear" :min="1970" :max="2100" controls-position="right" />
          <span class="tip">用于首页「N 年写作」统计</span>
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model="form.icp" placeholder="留空则页脚不显示" />
        </el-form-item>
      </div>

      <div class="group">
        <h3>首页 Hero</h3>
        <el-form-item label="大标题第一行">
          <el-input v-model="form.heroTitleLine1" />
        </el-form-item>
        <el-form-item label="大标题第二行">
          <el-input v-model="form.heroTitleLine2" />
          <span class="tip">第二行在前台渲染成灰色弱化</span>
        </el-form-item>
        <el-form-item label="首页描述">
          <el-input v-model="form.heroText" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="首页简介">
          <el-input
              v-model="aboutLinesText"
              type="textarea"
              :rows="6"
              placeholder="一行一段，按顺序显示在首页「关于我」区"
          />
        </el-form-item>
      </div>

      <div class="group">
        <h3>关于我（/aboutme）</h3>
        <el-form-item label="肖像图">
          <div class="portrait-row">
            <el-input v-model="form.portrait" placeholder="留空则用站名首字母占位" />
            <el-button size="small" @click="pickPortrait">上传</el-button>
            <img v-if="form.portrait" :src="resolveAsset(form.portrait)" alt="肖像预览" class="portrait-thumb">
          </div>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.profile.name" />
        </el-form-item>
        <el-form-item label="身份">
          <el-input v-model="form.profile.identity" />
        </el-form-item>
        <el-form-item label="座右铭">
          <el-input v-model="form.profile.motto" />
        </el-form-item>
        <el-form-item label="自我介绍">
          <el-input
              v-model="bioText"
              type="textarea"
              :rows="4"
              placeholder="一行一段（留空则用「关于我编辑」页的 Markdown 正文）"
          />
        </el-form-item>
        <el-form-item label="技术栈">
          <el-input
              v-model="skillsText"
              type="textarea"
              :rows="4"
              placeholder="每行一项：名称 | 说明（例如：Java | 后端主力语言）"
          />
        </el-form-item>
        <el-form-item label="兴趣爱好">
          <el-input v-model="interestsText" type="textarea" :rows="3" placeholder="每行一项" />
        </el-form-item>
        <el-form-item label="喜欢的作品">
          <el-input v-model="favoritesText" type="textarea" :rows="3" placeholder="每行一项（书 / 动漫 / 游戏 / 电影 / 音乐均可）" />
        </el-form-item>
        <el-form-item label="经历">
          <el-input
              v-model="journeyText"
              type="textarea"
              :rows="4"
              placeholder="每行一项：时间 | 事件（例如：2024.09 | 进入大连理工大学）"
          />
        </el-form-item>
      </div>

      <div class="group">
        <h3>社交 / 联系方式</h3>
        <el-form-item label="GitHub">
          <el-input v-model="form.socials.github" placeholder="留空则前台不显示该图标" />
        </el-form-item>
        <el-form-item label="Bilibili">
          <el-input v-model="form.socials.bilibili" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.socials.email" placeholder="用于 mailto 与 RSS 联系入口" />
        </el-form-item>
        <el-form-item label="QQ">
          <el-input v-model="form.socials.qq" />
        </el-form-item>
      </div>
    </el-form>

    <input ref="portraitInput" type="file" accept="image/*" class="hidden-input" @change="onPortraitPicked">
  </div>
</template>

<script>
import request, { resolveAsset } from '@/utils/request'
import { siteState, saveSiteConfig, resetSiteConfig } from '@/store/site'

export default {
  name: 'SiteEditor',
  data() {
    return {
      form: this.emptyForm(),
      aboutLinesText: '',
      bioText: '',
      skillsText: '',
      interestsText: '',
      favoritesText: '',
      journeyText: '',
      saving: false,
      loadedFromServer: false
    }
  },
  computed: {
    configured() {
      return !!siteState.configured
    }
  },
  created() {
    this.load()
  },
  methods: {
    resolveAsset,
    emptyForm() {
      return {
        name: '',
        author: '',
        slogan: '',
        identity: '',
        startYear: siteState.startYear || null,
        icp: '',
        heroTitleLine1: '',
        heroTitleLine2: '',
        heroText: '',
        portrait: '',
        socials: { github: '', bilibili: '', email: '', qq: '' },
        profile: {
          name: '',
          identity: '',
          motto: '',
          bio: [],
          skills: [],
          interests: [],
          favorites: [],
          journey: []
        }
      }
    },
    load() {
      request.get('/site').then(res => {
        if (res.code !== '200') {
          this.$message.error(res.msg || '站点配置读取失败')
          return
        }
        const data = res.data || {}
        this.loadedFromServer = true
        this.fillForm(data)
      }).catch(() => {
        this.$message.error('站点配置读取失败，请确认后端已启动')
      })
    },
    fillForm(data) {
      const socials = data.socials || {}
      const profile = data.profile || {}
      this.form = {
        name: data.name || '',
        author: data.author || '',
        slogan: data.slogan || '',
        identity: data.identity || '',
        startYear: data.startYear || siteState.startYear || null,
        icp: data.icp || '',
        heroTitleLine1: data.heroTitleLine1 || '',
        heroTitleLine2: data.heroTitleLine2 || '',
        heroText: data.heroText || '',
        portrait: data.portrait || '',
        socials: {
          github: socials.github || '',
          bilibili: socials.bilibili || '',
          email: socials.email || '',
          qq: socials.qq || ''
        },
        profile: {
          name: profile.name || '',
          identity: profile.identity || '',
          motto: profile.motto || '',
          bio: profile.bio || [],
          skills: profile.skills || [],
          interests: profile.interests || [],
          favorites: profile.favorites || [],
          journey: profile.journey || []
        }
      }
      this.aboutLinesText = (data.aboutLines || []).join('\n')
      this.bioText = (profile.bio || []).join('\n')
      this.skillsText = (profile.skills || [])
          .map(item => [item.label, item.desc].filter(v => v).join(' | '))
          .join('\n')
      this.interestsText = (profile.interests || []).join('\n')
      this.favoritesText = (profile.favorites || []).join('\n')
      this.journeyText = (profile.journey || [])
          .map(item => [item.period, item.text].filter(v => v).join(' | '))
          .join('\n')
    },

    /* ---------- 文本 ↔ 数组 ---------- */
    toLines(text) {
      return (text || '')
          .split('\n')
          .map(line => line.trim())
          .filter(line => line)
    },
    toPairs(text, firstKey, secondKey) {
      return this.toLines(text).map(line => {
        const index = line.search(/[|｜]/)
        const row = {}
        if (index === -1) {
          row[firstKey] = line
        } else {
          row[firstKey] = line.slice(0, index).trim()
          row[secondKey] = line.slice(index + 1).trim()
        }
        return row
      }).filter(row => row[firstKey] || row[secondKey])
    },

    buildPayload() {
      return {
        name: this.form.name,
        author: this.form.author,
        slogan: this.form.slogan,
        identity: this.form.identity,
        startYear: this.form.startYear,
        icp: this.form.icp,
        heroTitleLine1: this.form.heroTitleLine1,
        heroTitleLine2: this.form.heroTitleLine2,
        heroText: this.form.heroText,
        portrait: this.form.portrait,
        aboutLines: this.toLines(this.aboutLinesText),
        socials: { ...this.form.socials },
        profile: {
          name: this.form.profile.name,
          identity: this.form.profile.identity,
          motto: this.form.profile.motto,
          bio: this.toLines(this.bioText),
          skills: this.toPairs(this.skillsText, 'label', 'desc'),
          interests: this.toLines(this.interestsText),
          favorites: this.toLines(this.favoritesText),
          journey: this.toPairs(this.journeyText, 'period', 'text')
        }
      }
    },
    submit() {
      this.saving = true
      saveSiteConfig(this.buildPayload()).then(res => {
        if (res.code === '200') {
          this.$message.success('站点信息已保存，前台刷新即可看到')
        } else {
          this.$message.error(res.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('保存失败，请检查后端服务')
      }).finally(() => {
        this.saving = false
      })
    },
    resetAll() {
      this.$confirm('确定清空后台配置、恢复 src/config/site.js 里的代码默认值吗？',
          '提示', { type: 'warning' }).then(() => {
        resetSiteConfig().then(res => {
          if (res.code === '200') {
            this.$message.success('已恢复代码默认值')
            this.fillForm({})
          } else {
            this.$message.error(res.msg || '操作失败')
          }
        })
      }).catch(() => {})
    },
    openFront() {
      window.open('/', '_blank')
    },

    /* ---------- 肖像上传 ---------- */
    pickPortrait() {
      this.$refs.portraitInput.click()
    },
    onPortraitPicked(event) {
      const file = event.target.files && event.target.files[0]
      event.target.value = ''
      if (!file) {
        return
      }
      const formData = new FormData()
      formData.append('file', file)
      const loading = this.$message({ message: '上传中…', duration: 0 })
      request.post('/file/upload', formData).then(res => {
        if (res.code === '200') {
          this.form.portrait = res.data
          this.$message.success('已上传，记得点保存')
        } else {
          this.$message.error(res.msg || '上传失败')
        }
      }).catch(() => {
        this.$message.error('上传失败，请检查后端服务')
      }).finally(() => {
        loading.close()
      })
    }
  }
}
</script>

<style scoped>
.site-page {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.mb {
  margin-bottom: 16px;
}

.site-form {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px 20px 4px;
}

.group {
  padding-bottom: 8px;
  margin-bottom: 16px;
  border-bottom: 1px dashed #ebeef5;
}

.group:last-child {
  border-bottom: none;
}

.group h3 {
  margin: 8px 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.tip {
  margin-left: 10px;
  font-size: 12px;
  color: #a8abb2;
}

.portrait-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.portrait-thumb {
  height: 40px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.hidden-input {
  display: none;
}
</style>
