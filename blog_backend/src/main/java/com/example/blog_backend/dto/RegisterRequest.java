package com.example.blog_backend.dto;

/**
 * 普通用户注册请求。
 * website 为蜜罐字段（灌水机器人才会填，命中静默丢弃）。
 */
public class RegisterRequest {

    private String email;

    private String nickname;

    private String password;

    private String website;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
