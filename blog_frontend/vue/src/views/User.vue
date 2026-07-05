<template>
  <div class="user-page">
    <div class="page-header">
      <div>
        <h2>账号管理</h2>
      </div>
      <el-button type="primary" @click="add">新增账号</el-button>
    </div>

    <div class="toolbar">
      <el-input
          v-model="username"
          placeholder="请输入账号"
          clearable
          class="search-input"
          @keyup.enter.native="load"
          @clear="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table
        :data="tableData"
        border
        stripe
        class="user-table"
    >
      <el-table-column prop="username" label="账号" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />

      <el-table-column label="角色" width="130">
        <template slot-scope="scope">
          <el-tag
              :type="scope.row.role === 'SUPER_ADMIN' ? 'danger' : 'success'"
              size="small"
          >
            {{ getRoleText(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="170">
        <template slot-scope="scope">
          <span>{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="edit(scope.row)">编辑</el-button>
          <el-button
              type="danger"
              size="mini"
              :disabled="isSelf(scope.row)"
              @click="del(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />

    <el-dialog
        :title="form.id ? '编辑账号' : '新增账号'"
        :visible.sync="dialogVisible"
        width="460px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input
              v-model="form.username"
              placeholder="请输入账号"
              :disabled="Boolean(form.id)"
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="角色">
          <el-select v-model="form.role" class="form-select">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              :placeholder="form.id ? '留空则不修改密码' : '请输入密码'"
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
  name: 'User',
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 5,
      username: '',
      form: {},
      dialogVisible: false,
      currentUser: null
    }
  },
  created() {
    this.currentUser = this.getStoredUser()
    this.load()
  },
  methods: {
    load() {
      request.get('/user/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username
        }
      }).then(res => {
        if (res.code === '200') {
          this.tableData = res.data.records || []
          this.total = res.data.total || 0
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    reset() {
      this.username = ''
      this.pageNum = 1
      this.load()
    },
    add() {
      this.form = {
        username: '',
        password: '',
        nickname: '',
        email: '',
        role: 'ADMIN'
      }
      this.dialogVisible = true
    },
    edit(data) {
      this.form = {
        ...data,
        password: ''
      }
      this.dialogVisible = true
    },
    submit() {
      if (!this.form.username) {
        this.$message.warning('请输入账号')
        return
      }

      if (!this.form.id && !this.form.password) {
        this.$message.warning('请输入密码')
        return
      }

      request.post('/user', this.form).then(res => {
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
      this.$confirm('确定删除这个账号吗？', '提示', {
        type: 'warning'
      }).then(() => {
        request.delete('/user/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$message.success('删除成功')
            this.pageNum = 1
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.pageNum = 1
      this.load()
    },
    getRoleText(role) {
      if (role === 'SUPER_ADMIN') {
        return '超级管理员'
      }
      if (role === 'ADMIN') {
        return '管理员'
      }
      return role || '未知'
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
    },
    isSelf(user) {
      return this.currentUser && this.currentUser.id === user.id
    },
    getStoredUser() {
      try {
        return JSON.parse(localStorage.getItem('blog_user') || 'null')
      } catch (e) {
        return null
      }
    }
  }
}
</script>

<style scoped>
.user-page {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.page-header h2 {
  margin: 0;
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

.user-table {
  width: 100%;
}

.pagination {
  margin-top: 14px;
}

.form-select {
  width: 100%;
}
</style>
