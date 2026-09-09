<template>
  <div class="about-editor">
    <div class="page-header">
      <h2>关于我 · 编辑正文（Markdown）</h2>
      <div class="header-actions">
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </div>
    <p class="tip">正文支持 Markdown（加粗 / 链接 / 列表 / 代码 / 行内与块级公式 $…$、$$…$$ 等）。此处内容会覆盖 site.js 里的简介正文；置空则回退到 site.js 的默认文案。</p>

    <div class="editor-split">
      <div class="pane">
        <div class="pane-label">编辑（Markdown）</div>
        <textarea
            v-model="content"
            class="editor-input"
            placeholder="在这里用 Markdown 写“关于我”正文…"
        ></textarea>
      </div>
      <div class="pane">
        <div class="pane-label">预览</div>
        <div class="preview-scroll">
          <div v-if="previewHtml" class="markdown-body" v-html="previewHtml"></div>
          <div v-else class="preview-empty">暂无内容</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { renderMarkdown } from '@/utils/markdown'

export default {
  name: 'AboutEditor',
  data() {
    return {
      content: '',
      saving: false
    }
  },
  computed: {
    previewHtml() {
      return renderMarkdown(this.content)
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get('/about').then(res => {
        if (res.code === '200') {
          this.content = res.data || ''
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(() => {
        this.$message.error('加载失败，请确认后端已启动')
      })
    },
    save() {
      this.saving = true
      request.put('/about', { content: this.content }).then(res => {
        if (res.code === '200') {
          this.$message.success('已保存')
        } else {
          this.$message.error(res.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('网络错误，请稍后再试')
      }).finally(() => {
        this.saving = false
      })
    }
  }
}
</script>

<style scoped>
.about-editor {
  padding: 4px;
  display: flex;
  flex-direction: column;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.tip {
  margin: 8px 0 14px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
.editor-split {
  display: flex;
  gap: 12px;
  height: calc(100vh - 230px);
  min-height: 420px;
}
.pane {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}
.pane-label {
  padding: 8px 12px;
  font-size: 12px;
  color: #909399;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}
.editor-input {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  padding: 12px;
  font-family: Consolas, Menlo, monospace;
  font-size: 13px;
  line-height: 1.7;
}
.preview-scroll {
  flex: 1;
  overflow: auto;
  padding: 12px 16px;
}
.preview-empty {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
