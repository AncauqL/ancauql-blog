<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin-bottom: 10px">
      <el-input v-model="userName"
                placeholder="请输入姓名" style="width: 200px;margin-right:
  10px"></el-input>
      <el-button type="warning"
                 @click="load">查询</el-button>
      <el-button type="info"
                 @click="reset">重置</el-button>
      <el-button type="primary" @click="add"
                 style="float: right">新增</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" style="width:
  100%;margin-bottom: 10px">
      <el-table-column prop="userName" label="姓名"
                       width="180"></el-table-column>
      <el-table-column prop="age"
                       label="年龄"></el-table-column>
      <el-table-column prop="sex"
                       label="性别"></el-table-column>
      <el-table-column prop="phone"
                       label="电话"></el-table-column>
      <el-table-column prop="address"
                       label="地址"></el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="primary"
                     @click="edit(scope.row)">编辑</el-button>
          <el-button type="danger"
                     @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
    </el-pagination>

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="请填写信息"
               :visible.sync="dialogVisible" width="30%">
      <el-form ref="form" :model="form"
               label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.userName"
                    placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input v-model="form.age"
                    placeholder="请输入年龄"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio v-model="form.sex"
                    label="男"></el-radio>
          <el-radio v-model="form.sex"
                    label="女"></el-radio>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone"
                    placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address"
                    placeholder="请输入地址"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"
                     @click="submit">提交</el-button>
          <el-button @click="dialogVisible =
  false">取消</el-button>
        </el-form-item>
      </el-form>
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
      userName: '',
      form: {},
      dialogVisible: false
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      request.get("/user/selectPage", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          userName: this.userName,
        }
      }).then(res => {
        if (res.code == '200') {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      })
    },
    reset() {
      this.userName = ''
      this.pageNum = 1
      this.load()
    },
    add() {
      this.form = {}
      this.dialogVisible = true
    },
    edit(data) {
      this.form = { ...data }
      this.dialogVisible = true
    },
    submit() {
      request.post("/user", this.form).then(res => {
        if (res.code == '200') {
          this.$message.success('提交成功')
          this.dialogVisible = false
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {
      request.delete("/user/delete?id=" + id).then(res =>
      {
        if (res.code == '200') {
          this.$message.success('删除成功')
          this.pageNum = 1
          this.load()
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    }
  }
}
</script>