<template>
  <div class="tag-page">
    <div class="page-header">
      <div>
        <h2>标签管理</h2>
        <p>标签可在文章编辑器里挂到文章上，前台按标签筛选文章</p>
      </div>
      <el-button type="primary" @click="add">新增标签</el-button>
    </div>

    <div class="tag-list">
      <div v-for="item in tagList" :key="item.id" class="tag-item">
        <div class="tag-main">
          <div class="tag-name">{{ item.name }}</div>
          <div class="tag-count">{{ item.count }} 篇已发布文章</div>
        </div>

        <div class="tag-actions">
          <el-button type="primary" size="mini" @click="edit(item)">编辑</el-button>
          <el-button type="danger" size="mini" @click="del(item)">删除</el-button>
        </div>
      </div>

      <el-empty v-if="tagList.length === 0" description="暂无标签" />
    </div>

    <el-dialog title="标签信息" :visible.sync="dialogVisible" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标签名">
          <el-input
              v-model="form.name"
              maxlength="50"
              show-word-limit
              placeholder="例如：Java、算法、生活"
              @keyup.enter.native="submit"
          />
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Tag',
  data() {
    return {
      tagList: [],
      dialogVisible: false,
      form: {}
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get('/tag/selectAll').then(res => {
        if (res.code === '200') {
          this.tagList = res.data || []
        }
      })
    },
    add() {
      this.form = { name: '' }
      this.dialogVisible = true
    },
    edit(item) {
      this.form = { id: item.id, name: item.name }
      this.dialogVisible = true
    },
    submit() {
      const name = (this.form.name || '').trim()
      if (!name) {
        this.$message.warning('请输入标签名')
        return
      }

      request.post('/tag', { id: this.form.id, name }).then(res => {
        if (res.code === '200') {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(item) {
      const tip = item.count > 0
        ? `标签「${item.name}」还挂着 ${item.count} 篇文章，删除后这些文章的该标签会一并移除，确定删除吗？`
        : `确定删除标签「${item.name}」吗？`
      this.$confirm(tip, '提示', { type: 'warning' }).then(() => {
        request.delete('/tag/delete?id=' + item.id).then(res => {
          if (res.code === '200') {
            this.$message.success('删除成功')
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.tag-page {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
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
}

.tag-list {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #ebeef5;
}

.tag-item:last-child {
  border-bottom: none;
}

.tag-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.tag-count {
  font-size: 13px;
  color: #909399;
}

.tag-actions {
  white-space: nowrap;
}
</style>
