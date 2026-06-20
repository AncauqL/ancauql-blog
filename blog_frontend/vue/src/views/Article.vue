<template>
  <div class="article-page">
    <div class="page-header">
      <div>
        <h2>文章管理</h2>
      </div>
      <el-button type="primary" @click="add">新增文章</el-button>
    </div>

    <div class="toolbar">
      <el-input
          v-model="searchTitle"
          placeholder="请输入文章标题"
          clearable
          class="search-input"
          @keyup.enter.native="load"
          @clear="load"
      />
      <el-select
          v-model="searchStatus"
          placeholder="文章状态"
          clearable
          class="status-select"
      >
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table
        :data="filteredArticleList"
        border
        stripe
        class="article-table"
    >
      <el-table-column prop="title" label="标题" min-width="180" />

      <el-table-column label="摘要" min-width="240">
        <template slot-scope="scope">
          <span>{{ scope.row.summary || '暂无摘要' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="分类" width="120">
        <template slot-scope="scope">
          <span>{{ getCategoryName(scope.row.categoryId) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag
              :type="scope.row.status === 'published' ? 'success' : 'info'"
              size="small"
          >
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

<!--      <el-table-column prop="viewCount" label="浏览量" width="90" />-->

      <el-table-column label="创建时间" width="170">
        <template slot-scope="scope">
          <span>{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="edit(scope.row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
        v-if="filteredArticleList.length === 0"
        description="暂无文章"
    />

    <el-dialog
        :title="form.id ? '编辑文章' : '新增文章'"
        :visible.sync="dialogVisible"
        width="760px"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="文章标题">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="文章摘要">
          <el-input
              v-model="form.summary"
              type="textarea"
              :rows="2"
              placeholder="请输入文章摘要"
          />
        </el-form-item>

        <el-form-item label="文章分类">
          <el-select
              v-model="form.categoryId"
              placeholder="请选择文章分类"
              clearable
              class="form-select"
          >
            <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面地址">
          <el-input v-model="form.cover" placeholder="请输入封面图片地址，可留空" />
        </el-form-item>

        <el-form-item label="文章状态">
          <el-radio-group v-model="form.status">
            <el-radio label="published">已发布</el-radio>
            <el-radio label="draft">草稿</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="文章正文">
          <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              placeholder="请输入文章正文"
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
  name: 'Article',
  data() {
    return {
      articleList: [],
      categoryList: [],
      searchTitle: '',
      searchStatus: '',
      dialogVisible: false,
      form: {}
    }
  },
  computed: {
    filteredArticleList() {
      if (!this.searchStatus) {
        return this.articleList
      }

      return this.articleList.filter(item => item.status === this.searchStatus)
    }
  },
  created() {
    this.loadCategories()
    this.load()
  },
  methods: {
    load() {
      const articleTitle = this.searchTitle || ''
      const requestTask = articleTitle
          ? request.get('/article/selectSearch', {
            params: { articleTitle }
          })
          : request.get('/article/selectAll')

      requestTask.then(res => {
        if (res.code === '200') {
          this.articleList = res.data || []
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadCategories() {
      request.get('/category/selectAll').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data || []
        }
      })
    },
    resetSearch() {
      this.searchTitle = ''
      this.searchStatus = ''
      this.load()
    },
    add() {
      this.form = {
        title: '',
        summary: '',
        content: '',
        cover: '',
        categoryId: null,
        userId: 1,
        status: 'published',
        viewCount: 0
      }
      this.dialogVisible = true
    },
    edit(item) {
      this.form = { ...item }
      this.dialogVisible = true
    },
    submit() {
      if (!this.form.title) {
        this.$message.warning('请输入文章标题')
        return
      }

      if (!this.form.content) {
        this.$message.warning('请输入文章正文')
        return
      }

      request.post('/article', this.form).then(res => {
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
      this.$confirm('确定删除这篇文章吗？', '提示', {
        type: 'warning'
      }).then(() => {
        request.delete('/article/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$message.success('删除成功')
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    },
    getCategoryName(categoryId) {
      const category = this.categoryList.find(item => item.id === categoryId)
      return category ? category.name : '未分类'
    },
    getStatusText(status) {
      if (status === 'published') {
        return '已发布'
      }
      if (status === 'draft') {
        return '草稿'
      }
      return '未知'
    },
    formatTime(value) {
      if (!value) {
        return '-'
      }

      if (Array.isArray(value)) {
        const year = value[0]
        const month = this.padZero(value[1])
        const day = this.padZero(value[2])
        const hour = this.padZero(value[3] || 0)
        const minute = this.padZero(value[4] || 0)
        return `${year}-${month}-${day} ${hour}:${minute}`
      }

      return String(value).replace('T', ' ').slice(0, 16)
    },
    padZero(value) {
      return String(value).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
.article-page {
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.search-input {
  width: 240px;
}

.status-select {
  width: 140px;
}

.article-table {
  width: 100%;
}

.form-select {
  width: 100%;
}
</style>
