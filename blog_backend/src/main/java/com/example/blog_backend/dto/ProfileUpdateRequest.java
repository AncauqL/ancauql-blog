package com.example.blog_backend.dto;

/**
 * 当前登录用户更新资料请求（绑定/更换邮箱、修改密码）。
 * currentPassword 必填用于验证身份；email / newPassword 至少给一个。
 */
public class ProfileUpdateRequest {

    private String email;
    private String currentPassword;
    private String newPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
