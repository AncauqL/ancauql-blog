<template>
  <div class="settings-page">
    <div class="page-header">
      <h2>账号设置</h2>
    </div>

    <div class="settings-card">
      <p class="account-line">当前账号：<strong>{{ username }}</strong></p>
      <p class="tip">在此绑定/更换邮箱、修改密码。改密码需先输入当前密码；保存后可用新邮箱或用户名登录。</p>

      <el-form
          :model="form"
          label-position="top"
          class="settings-form"
      >
        <el-form-item label="邮箱（绑定 / 更换）">
          <el-input
              v-model="form.email"
              type="email"
              placeholder="you@example.com"
              clearable
          />
        </el-form-item>

        <el-form-item label="当前密码（必填，用于验证）">
          <el-input
              v-model="form.currentPassword"
              type="password"
              placeholder="输入当前密码"
              show-password
          />
        </el-form-item>

        <el-form-item label="新密码（留空则不修改，至少 8 位）">
          <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="留空则不修改"
              show-password
          />
        </el-form-item>

        <el-form-item label="确认新密码">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              show-password
          />
        </el-form-item>

        <el-button
            type="primary"
            :loading="saving"
            @click="save"
        >
          保存
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { getStoredUser } from '@/utils/auth'

export default {
  name: 'AccountSettings',
  data() {
    const user = getStoredUser() || {}
    return {
      username: user.username || '',
      saving: false,
      form: {
        email: (user.email && user.email.trim()) ? user.email : '',
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    save() {
      if (!this.form.currentPassword) {
        this.$message.warning('请输入当前密码以验证身份')
        return
      }
      if (this.form.newPassword && this.form.newPassword !== this.form.confirmPassword) {
        this.$message.warning('两次输入的新密码不一致')
        return
      }
      if (this.form.newPassword && this.form.newPassword.length < 8) {
        this.$message.warning('新密码至少 8 位')
        return
      }
      this.saving = true
      const payload = {
        email: this.form.email.trim() || null,
        currentPassword: this.form.currentPassword,
        newPassword: this.form.newPassword || null
      }
      request.post('/auth/profile', payload).then(res => {
        if (res.code === '200') {
          this.$message.success('已保存')
          if (res.data) {
            localStorage.setItem('blog_user', JSON.stringify(res.data))
            this.form.email = (res.data.email && res.data.email.trim()) ? res.data.email : ''
          }
          this.form.currentPassword = ''
          this.form.newPassword = ''
          this.form.confirmPassword = ''
        } else {
          this.$message.error(res.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('网络错误，请稍后再试')
      }).finally(() => {
        this.saving = false
      })
    }
  }
}
</script>

<style scoped>
.settings-page {
  padding: 4px;
  max-width: 520px;
}
.page-header h2 {
  margin: 0 0 16px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.settings-card {
  padding: 20px 22px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}
.account-line {
  margin: 0 0 8px;
  color: #303133;
}
.tip {
  margin: 0 0 18px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
