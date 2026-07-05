<template>
  <section class="login-page">
    <div class="login-panel">
      <h1>后台登录</h1>

      <el-form
          :model="form"
          label-position="top"
          @submit.native.prevent
      >
        <el-form-item label="账号">
          <el-input
              v-model="form.username"
              placeholder="请输入账号"
              clearable
              @keyup.enter.native="submit"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              @keyup.enter.native="submit"
          />
        </el-form-item>

        <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="submit"
        >
          登录
        </el-button>
      </el-form>
    </div>
  </section>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    submit() {
      if (!this.form.username || !this.form.password) {
        this.$message.warning('请输入账号和密码')
        return
      }

      this.loading = true
      request.post('/auth/login', this.form).then(res => {
        if (res.code === '200') {
          localStorage.setItem('blog_token', res.data.token)
          localStorage.setItem('blog_user',
              JSON.stringify(res.data.user))
          this.$message.success('登录成功')
          this.$router.replace(this.$route.query.redirect || '/article')
        } else {
          this.$message.error(res.msg)
        }
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #303133;
}

.login-panel {
  width: 360px;
  max-width: 100%;
  padding: 28px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}

.login-panel h1 {
  margin: 0 0 24px;
  font-size: 24px;
  line-height: 1.4;
}

.login-button {
  width: 100%;
}
</style>
