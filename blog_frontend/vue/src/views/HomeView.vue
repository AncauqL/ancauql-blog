<template>
  <div>
  <div style="margin-bottom: 10px">
    <el-input v-model="input" placeholder="请输入内容"
              style="width: 200px;margin-right: 10px"></el-input>
    <el-button type="warning">查询</el-button>
    <el-button type="success">新增</el-button>
  </div>

  <el-table :data="tableData" stripe style="width:
  100%;margin-bottom: 10px">
    <el-table-column prop="date" label="日期"
                     width="180"></el-table-column>
    <el-table-column prop="name" label="姓名"
                     width="180"></el-table-column>
    <el-table-column prop="address" label="地址"></el-table-column>
    <el-table-column label="操作">
      <el-button type="primary">编辑</el-button>
      <el-button type="danger">删除</el-button>
    </el-table-column>
  </el-table>

  <div>
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[100, 200, 300, 400]"
        :page-size="100"
        layout="total, sizes, prev, pager, next, jumper"
        :total="400">
    </el-pagination>
  </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      input: '',
      currentPage: 1,
      tableData: [
        {
          date: '2016-05-02',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1518 弄'
        },
        {
          date: '2016-05-04',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1517 弄'
        },
        {
          date: '2016-05-01',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1519 弄'
        },
        {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄'
        }
      ]
    }
  },
  methods: {
    handleSizeChange(val) {
      console.log('每页条数变为', val)
    },
    handleCurrentChange(val) {
      console.log('当前页码变为', val)
    }
  }
}
</script>
