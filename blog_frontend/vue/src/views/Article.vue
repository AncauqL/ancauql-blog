<template>
  <div class="article-page">
    <div class="page-header">
      <div>
        <h2>文章管理</h2>
      </div>
      <el-button type="primary" @click="$router.push('/article/edit')">
        新增文章
      </el-button>
    </div>

    <div class="toolbar">
      <el-input
          v-model="searchTitle"
          placeholder="请输入文章标题"
          clearable
          class="search-input"
          @keyup.enter.native="search"
          @clear="search"
      />
      <el-select
          v-model="searchStatus"
          placeholder="文章状态"
          clearable
          class="status-select"
          @change="search"
      >
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table
        :data="articleList"
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

      <el-table-column prop="viewCount" label="阅读" width="80" />

      <el-table-column label="创建时间" width="170">
        <template slot-scope="scope">
          <span>{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <el-button
              type="primary"
              size="mini"
              @click="$router.push('/article/edit/' + scope.row.id)"
          >
            编辑
          </el-button>
          <el-button type="danger" size="mini" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
        v-if="articleList.length === 0"
        description="暂无文章"
    />

    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="onPageChange"
      />
    </div>
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
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.loadCategories()
    this.load()
  },
  methods: {
    load() {
      request.get('/article/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          articleTitle: this.searchTitle || '',
          status: this.searchStatus || ''
        }
      }).then(res => {
        if (res.code === '200') {
          this.articleList = (res.data && res.data.records) || []
          this.total = (res.data && res.data.total) || 0
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    search() {
      this.pageNum = 1
      this.load()
    },
    onPageChange(page) {
      this.pageNum = page
      this.load()
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
      this.search()
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

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
