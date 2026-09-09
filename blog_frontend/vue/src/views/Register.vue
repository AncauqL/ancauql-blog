<template>
  <section class="login-page">
    <div class="login-panel">
      <h1>注册账号</h1>
      <p class="login-tip">注册后可用邮箱登录、发表评论；普通账号没有后台权限</p>

      <el-form
          :model="form"
          label-position="top"
          @submit.native.prevent
      >
        <!-- 蜜罐：视觉隐藏，灌水机器人才会填 -->
        <input v-model="form.website" type="text" class="honeypot" name="website" tabindex="-1" autocomplete="off">

        <el-form-item label="邮箱">
          <el-input
              v-model="form.email"
              type="email"
              placeholder="you@example.com"
              clearable
          />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
              v-model="form.nickname"
              placeholder="评论时显示的名字"
              maxlength="40"
              clearable
          />
        </el-form-item>

        <el-form-item label="密码（至少 8 位）">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请设置密码"
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
          注册并登录
        </el-button>

        <div class="login-footer">
          已有账号？
          <router-link to="/login">去登录</router-link>
        </div>
      </el-form>
    </div>
  </section>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Register',
  data() {
    return {
      loading: false,
      form: {
        email: '',
        nickname: '',
        password: '',
        website: ''
      }
    }
  },
  methods: {
    submit() {
      if (!this.form.email || !this.form.nickname) {
        this.$message.warning('请填写邮箱和昵称')
        return
      }
      if (!this.form.password || this.form.password.length < 8) {
        this.$message.warning('密码至少 8 位')
        return
      }
      this.loading = true
      request.post('/auth/register', this.form).then(res => {
        if (res.code === '200') {
          localStorage.setItem('blog_token', res.data.token)
          localStorage.setItem('blog_user', JSON.stringify(res.data.user))
          this.$message.success('注册成功，欢迎你')
          this.$router.replace(this.$route.query.redirect || '/')
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
  width: 380px;
  max-width: 100%;
  padding: 28px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}
.login-panel h1 {
  margin: 0 0 8px;
  font-size: 24px;
  line-height: 1.4;
}
.login-tip {
  margin: 0 0 20px;
  font-size: 12px;
  color: #909399;
}
.login-button {
  width: 100%;
}
.login-footer {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
}
.login-footer a {
  color: #409eff;
  text-decoration: none;
}
.honeypot {
  position: absolute;
  left: -9999px;
  top: -9999px;
  width: 1px;
  height: 1px;
  opacity: 0;
}
</style>
