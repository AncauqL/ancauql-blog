<template>
  <div class="editor-page">
    <!-- 顶栏：返回 / 标题 / 统计 / 保存动作 -->
    <div class="editor-topbar">
      <div class="topbar-left">
        <el-button type="text" icon="el-icon-back" @click="goBack">
          返回列表
        </el-button>
        <span class="topbar-title">
          {{ form.id ? '编辑文章' : '新文章' }}
        </span>
        <span v-if="wordCount" class="topbar-stat">
          {{ wordCount }} 字
        </span>
        <span v-if="draftSavedAt" class="topbar-stat">
          本地草稿已存 {{ draftSavedAt }}
        </span>
      </div>
      <div class="topbar-right">
        <el-radio-group v-model="form.status" size="small">
          <el-radio-button label="draft">草稿</el-radio-button>
          <el-radio-button label="published">发布</el-radio-button>
        </el-radio-group>
        <el-checkbox v-model="form.top" style="margin-left:12px">置顶</el-checkbox>
        <el-button size="small" :loading="saving" @click="save(false)">
          保存
        </el-button>
        <el-button
            type="primary"
            size="small"
            :loading="saving"
            @click="save(true)"
        >
          保存并返回
        </el-button>
      </div>
    </div>

    <!-- 元信息区 -->
    <div class="editor-meta">
      <el-input
          v-model="form.title"
          class="meta-title"
          placeholder="文章标题"
      />
      <div class="meta-row">
        <el-select
            v-model="form.categoryId"
            placeholder="选择分类"
            clearable
            class="meta-category"
        >
          <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
        <el-input
            v-model="form.summary"
            placeholder="摘要（列表页展示，建议填写）"
            class="meta-summary"
        />
        <div class="meta-cover">
          <el-input
              v-model="form.cover"
              placeholder="封面图地址"
              class="cover-input"
          />
          <el-button size="small" @click="pickCover">上传封面</el-button>
          <img
              v-if="form.cover"
              :src="resolveAsset(form.cover)"
              class="cover-thumb"
              alt="封面预览"
          >
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <el-button size="mini" @click="pickImage">插入图片</el-button>
      <el-button size="mini" @click="wrapSelection('**', '**', '加粗文字')">加粗</el-button>
      <el-button size="mini" @click="wrapSelection('`', '`', '代码')">行内代码</el-button>
      <el-button size="mini" @click="insertBlock('```java\n', '\n```', '// 代码')">代码块</el-button>
      <el-button size="mini" @click="wrapSelection('[', '](https://)', '链接文字')">链接</el-button>
      <el-button size="mini" @click="insertBlock('> ', '', '引用内容')">引用</el-button>
      <span class="toolbar-tip">
        支持直接粘贴 / 拖入图片自动上传 · Tab 缩进 · Ctrl+S 保存
      </span>
    </div>

    <!-- 分栏：左编辑 / 右预览 -->
    <div class="editor-split">
      <textarea
          ref="textarea"
          v-model="form.content"
          class="editor-input"
          placeholder="用 Markdown 开始写作..."
          spellcheck="false"
          @keydown.tab.prevent="insertTab"
          @keydown="onKeydown"
          @paste="onPaste"
          @drop.prevent="onDrop"
          @dragover.prevent
      ></textarea>
      <div class="editor-preview">
        <div class="markdown-body" v-html="renderedPreview"></div>
        <el-empty
            v-if="!form.content"
            description="预览区：左侧输入 Markdown 后实时渲染"
        />
      </div>
    </div>

    <!-- 隐藏的文件选择器 -->
    <input
        ref="imageInput"
        type="file"
        accept="image/jpeg,image/png,image/gif,image/webp"
        style="display: none"
        @change="onImagePicked"
    >
    <input
        ref="coverInput"
        type="file"
        accept="image/jpeg,image/png,image/gif,image/webp"
        style="display: none"
        @change="onCoverPicked"
    >
  </div>
</template>

<script>
import request, { resolveAsset } from '@/utils/request'
import { renderMarkdown, countWords } from '@/utils/markdown'

export default {
  name: 'ArticleEditor',
  data() {
    return {
      form: this.emptyForm(),
      categoryList: [],
      previewSource: '',
      previewTimer: null,
      autosaveTimer: null,
      draftSavedAt: '',
      savedSnapshot: '',
      saving: false,
      skipLeaveGuard: false
    }
  },
  computed: {
    renderedPreview() {
      return renderMarkdown(this.previewSource)
    },
    wordCount() {
      return countWords(this.form.content)
    },
    draftKey() {
      return 'blog_editor_draft_' + (this.form.id || 'new')
    },
    isDirty() {
      return JSON.stringify(this.form) !== this.savedSnapshot
    }
  },
  watch: {
    '$route.params.id'() {
      this.init()
    },
    'form.content'(value) {
      // 预览防抖，避免长文每敲一个字全文重渲染
      clearTimeout(this.previewTimer)
      this.previewTimer = setTimeout(() => {
        this.previewSource = value
      }, 300)
      this.scheduleAutosave()
    },
    'form.title'() {
      this.scheduleAutosave()
    },
    'form.summary'() {
      this.scheduleAutosave()
    }
  },
  created() {
    this.loadCategories()
    this.init()
  },
  beforeDestroy() {
    clearTimeout(this.previewTimer)
    clearTimeout(this.autosaveTimer)
  },
  beforeRouteLeave(to, from, next) {
    if (this.skipLeaveGuard || !this.isDirty) {
      next()
      return
    }
    this.$confirm('有未保存到服务器的修改（本地草稿已自动保留），确定离开吗？',
        '提示', { type: 'warning' })
        .then(() => next())
        .catch(() => next(false))
  },
  methods: {
    emptyForm() {
      return {
        title: '',
        summary: '',
        content: '',
        cover: '',
        categoryId: null,
        userId: 1,
        status: 'draft',
        top: false,
        viewCount: 0
      }
    },

    /* ---------- 初始化与加载 ---------- */
    init() {
      const id = this.$route.params.id
      if (id) {
        request.get('/article/detail', { params: { id } }).then(res => {
          if (res.code === '200') {
            this.form = { ...res.data }
            this.previewSource = this.form.content || ''
            this.savedSnapshot = JSON.stringify(this.form)
            this.tryRestoreDraft()
          } else {
            this.$message.error(res.msg || '文章加载失败')
            this.goBack()
          }
        })
      } else {
        this.form = this.emptyForm()
        this.previewSource = ''
        this.savedSnapshot = JSON.stringify(this.form)
        this.tryRestoreDraft()
      }
    },
    loadCategories() {
      request.get('/category/selectAll').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data || []
        }
      })
    },

    /* ---------- 本地草稿（防丢稿） ---------- */
    scheduleAutosave() {
      clearTimeout(this.autosaveTimer)
      this.autosaveTimer = setTimeout(() => {
        if (!this.isDirty) {
          return
        }
        const payload = {
          form: this.form,
          savedAt: Date.now()
        }
        localStorage.setItem(this.draftKey, JSON.stringify(payload))
        this.draftSavedAt = this.formatClock(payload.savedAt)
      }, 2000)
    },
    tryRestoreDraft() {
      let payload = null
      try {
        payload = JSON.parse(localStorage.getItem(this.draftKey) || 'null')
      } catch (e) {
        payload = null
      }
      if (!payload || !payload.form) {
        return
      }
      if (JSON.stringify(payload.form) === JSON.stringify(this.form)) {
        localStorage.removeItem(this.draftKey)
        return
      }
      const time = this.formatClock(payload.savedAt)
      this.$confirm(`检测到 ${time} 的本地未保存草稿，是否恢复？`, '本地草稿', {
        confirmButtonText: '恢复',
        cancelButtonText: '丢弃',
        type: 'info'
      }).then(() => {
        this.form = { ...this.form, ...payload.form }
        this.previewSource = this.form.content || ''
      }).catch(() => {
        localStorage.removeItem(this.draftKey)
      })
    },
    clearDraft() {
      localStorage.removeItem(this.draftKey)
      this.draftSavedAt = ''
    },

    /* ---------- 保存 ---------- */
    save(backAfterSave) {
      if (!this.form.title) {
        this.$message.warning('请输入文章标题')
        return
      }
      if (!this.form.content) {
        this.$message.warning('请输入文章正文')
        return
      }
      this.saving = true
      request.post('/article', this.form).then(res => {
        if (res.code !== '200') {
          this.$message.error(res.msg || '保存失败')
          return
        }
        this.$message.success(this.form.status === 'published'
            ? '已保存并发布' : '草稿已保存到服务器')
        this.clearDraft()
        const isNew = !this.form.id
        if (res.data && res.data.id) {
          this.form.id = res.data.id
        }
        this.savedSnapshot = JSON.stringify(this.form)
        if (backAfterSave) {
          this.skipLeaveGuard = true
          this.$router.push('/article')
        } else if (isNew && this.form.id) {
          // 新文章保存后把地址换成 /article/edit/{id}，刷新不丢
          this.skipLeaveGuard = true
          this.$router.replace('/article/edit/' + this.form.id)
          this.$nextTick(() => {
            this.skipLeaveGuard = false
            this.savedSnapshot = JSON.stringify(this.form)
          })
        }
      }).catch(() => {
        this.$message.error('保存失败，请检查后端服务')
      }).finally(() => {
        this.saving = false
      })
    },

    /* ---------- 图片上传 ---------- */
    uploadImage(file) {
      const formData = new FormData()
      formData.append('file', file)
      return request.post('/file/upload', formData).then(res => {
        if (res.code !== '200') {
          throw new Error(res.msg || '上传失败')
        }
        return res.data
      })
    },
    uploadAndInsert(file) {
      const loading = this.$message({
        message: '图片上传中...',
        duration: 0
      })
      this.uploadImage(file).then(url => {
        const name = (file.name || '图片').replace(/\.[^.]+$/, '')
        this.insertAtCursor(`![${name}](${url})\n`)
      }).catch(err => {
        this.$message.error(err.message || '图片上传失败')
      }).finally(() => {
        loading.close()
      })
    },
    pickImage() {
      this.$refs.imageInput.click()
    },
    onImagePicked(event) {
      const file = event.target.files[0]
      if (file) {
        this.uploadAndInsert(file)
      }
      event.target.value = ''
    },
    pickCover() {
      this.$refs.coverInput.click()
    },
    onCoverPicked(event) {
      const file = event.target.files[0]
      event.target.value = ''
      if (!file) {
        return
      }
      this.uploadImage(file).then(url => {
        this.form.cover = url
        this.$message.success('封面已上传')
      }).catch(err => {
        this.$message.error(err.message || '封面上传失败')
      })
    },
    onPaste(event) {
      const items = (event.clipboardData || {}).items || []
      for (const item of items) {
        if (item.kind === 'file' && item.type.startsWith('image/')) {
          event.preventDefault()
          this.uploadAndInsert(item.getAsFile())
          return
        }
      }
    },
    onDrop(event) {
      const files = (event.dataTransfer || {}).files || []
      for (const file of files) {
        if (file.type.startsWith('image/')) {
          this.uploadAndInsert(file)
          return
        }
      }
    },
    resolveAsset,

    /* ---------- 编辑辅助 ---------- */
    insertAtCursor(text) {
      const textarea = this.$refs.textarea
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const value = this.form.content || ''
      this.form.content = value.slice(0, start) + text + value.slice(end)
      this.$nextTick(() => {
        textarea.focus()
        textarea.selectionStart = textarea.selectionEnd = start + text.length
      })
    },
    wrapSelection(before, after, placeholder) {
      const textarea = this.$refs.textarea
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const value = this.form.content || ''
      const selected = value.slice(start, end) || placeholder
      const text = before + selected + after
      this.form.content = value.slice(0, start) + text + value.slice(end)
      this.$nextTick(() => {
        textarea.focus()
        textarea.selectionStart = start + before.length
        textarea.selectionEnd = start + before.length + selected.length
      })
    },
    insertBlock(before, after, placeholder) {
      const textarea = this.$refs.textarea
      const start = textarea.selectionStart
      const value = this.form.content || ''
      // 块级内容确保独占一行
      const needsNewline = start > 0 && value[start - 1] !== '\n'
      this.wrapSelection((needsNewline ? '\n' : '') + before, after, placeholder)
    },
    insertTab() {
      this.insertAtCursor('  ')
    },
    onKeydown(event) {
      if ((event.ctrlKey || event.metaKey) && event.key === 's') {
        event.preventDefault()
        this.save(false)
      }
    },

    /* ---------- 其他 ---------- */
    goBack() {
      this.$router.push('/article')
    },
    formatClock(timestamp) {
      const d = new Date(timestamp)
      const pad = v => String(v).padStart(2, '0')
      return `${pad(d.getHours())}:${pad(d.getMinutes())}`
    }
  }
}
</script>

<style scoped>
.editor-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  min-height: 480px;
}

.editor-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.topbar-stat {
  font-size: 12px;
  color: #909399;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.editor-meta {
  margin-top: 12px;
}

.meta-title >>> .el-input__inner {
  font-size: 20px;
  font-weight: 600;
  height: 44px;
}

.meta-row {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.meta-category {
  width: 180px;
}

.meta-summary {
  flex: 1;
  min-width: 240px;
}

.meta-cover {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cover-input {
  width: 220px;
}

.cover-thumb {
  height: 32px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 8px 10px;
  border: 1px solid #ebeef5;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
  background: #fafbfc;
  flex-wrap: wrap;
}

.toolbar-tip {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.editor-split {
  display: flex;
  flex: 1;
  min-height: 0;
  border: 1px solid #ebeef5;
  border-radius: 0 0 6px 6px;
  overflow: hidden;
}

.editor-input {
  flex: 1;
  min-width: 0;
  padding: 16px;
  border: none;
  outline: none;
  resize: none;
  font-size: 14.5px;
  line-height: 1.8;
  font-family: "JetBrains Mono", Consolas, "Courier New", monospace;
  color: #303133;
  background: #fff;
}

.editor-preview {
  flex: 1;
  min-width: 0;
  padding: 16px 20px;
  overflow-y: auto;
  border-left: 1px solid #ebeef5;
  background: #fcfdfe;
}

@media (max-width: 900px) {
  .editor-split {
    flex-direction: column;
  }

  .editor-preview {
    border-left: none;
    border-top: 1px solid #ebeef5;
  }
}
</style>
