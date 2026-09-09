<template>
  <div class="comment-page">
    <div class="page-header">
      <h2>评论管理</h2>
      <span class="total">{{ list.length }} 条</span>
    </div>

    <el-table
        v-if="list.length"
        :data="list"
        border
        stripe
        class="comment-table"
    >
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column label="内容" min-width="240">
        <template slot-scope="scope">
          <span class="content-cell">{{ scope.row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column label="文章" min-width="160">
        <template slot-scope="scope">
          <a class="article-link" @click="goPost(scope.row.articleId)">
            {{ articleTitle(scope.row.articleId) || ('文章 #' + scope.row.articleId) }}
          </a>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template slot-scope="scope">
          <span>{{ formatDateTime(scope.row.createTime) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="暂无评论" />
  </div>
</template>

<script>
import request from '@/utils/request'
import { formatDateTime } from '@/utils/datetime'

export default {
  name: 'CommentAdmin',
  data() {
    return {
      list: [],
      titleMap: {}
    }
  },
  created() {
    this.load()
    this.loadTitles()
  },
  methods: {
    formatDateTime,
    load() {
      request.get('/comment/list').then(res => {
        if (res.code === '200') {
          this.list = res.data || []
        } else {
          this.$message.error(res.msg)
        }
      }).catch(() => {
        this.$message.error('评论加载失败，请确认后端已启动')
      })
    },
    loadTitles() {
      // 用归档接口拿「已发布文章 id -> 标题」做展示映射（轻量，只含必要字段）
      request.get('/article/archive').then(res => {
        if (res.code === '200') {
          const map = {}
          ;(res.data || []).forEach(group => {
            ;(group.articles || []).forEach(item => {
              map[item.id] = item.title
            })
          })
          this.titleMap = map
        }
      }).catch(() => {})
    },
    articleTitle(id) {
      return this.titleMap[id] || ''
    },
    goPost(id) {
      window.open('/post/' + id, '_blank')
    },
    del(id) {
      this.$confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
        .then(() => {
          request.delete('/comment/delete?id=' + id).then(res => {
            if (res.code === '200') {
              this.$message.success('已删除')
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
.comment-page {
  padding: 4px;
}
.page-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.total {
  color: #909399;
  font-size: 13px;
}
.comment-table {
  width: 100%;
}
.content-cell {
  white-space: pre-wrap;
  word-break: break-word;
}
.article-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}
.article-link:hover {
  text-decoration: underline;
}
</style>
