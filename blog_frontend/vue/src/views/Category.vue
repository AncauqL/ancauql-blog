<template>
  <div class="category-page">
    <div class="page-header">
      <div>
        <h2>分类管理</h2>
      </div>
      <el-button type="primary" @click="add">新增分类</el-button>
    </div>

    <div class="category-list">
      <div
          v-for="item in sortedCategories"
          :key="item.id"
          class="category-item"
      >
        <div class="category-main">
          <div class="category-name">{{ item.name }}</div>
          <div class="category-description">
            {{ item.description || '暂无描述' }}
          </div>
        </div>

        <div class="category-meta">
          <span>排序：{{ item.sort || 0 }}</span>
          <el-button type="primary" size="mini" @click="edit(item)">编辑</el-button>
          <el-button type="danger" size="mini" @click="del(item.id)">删除</el-button>
        </div>
      </div>

      <el-empty
          v-if="categoryList.length === 0"
          description="暂无分类"
      />
    </div>

    <el-dialog
        title="分类信息"
        :visible.sync="dialogVisible"
        width="420px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="分类描述">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入分类描述"
          />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number
              v-model="form.sort"
              :min="0"
              :step="1"
              controls-position="right"
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
  name: 'Category',
  data() {
    return {
      categoryList: [],
      dialogVisible: false,
      form: {}
    }
  },
  computed: {
    sortedCategories() {
      return [...this.categoryList].sort((a, b) => {
        return (a.sort || 0) - (b.sort || 0)
      })
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get('/category/selectAll').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data
        }
      })
    },
    add() {
      this.form = {
        name: '',
        description: '',
        sort: 0
      }
      this.dialogVisible = true
    },
    edit(item) {
      this.form = { ...item }
      this.dialogVisible = true
    },
    submit() {
      if (!this.form.name) {
        this.$message.warning('请输入分类名称')
        return
      }

      request.post('/category', this.form).then(res => {
        if (res.code === '200') {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {
      this.$confirm('确定删除这个分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        request.delete('/category/delete?id=' + id).then(res => {
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
.category-page {
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
  font-size: 14px;
}

.category-list {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  border-bottom: 1px solid #ebeef5;
}

.category-item:last-child {
  border-bottom: none;
}

.category-main {
  min-width: 0;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.category-description {
  font-size: 14px;
  color: #606266;
}

.category-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #909399;
  white-space: nowrap;
}
</style>