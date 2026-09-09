package com.example.blog_backend.dto;

/** 保存 AboutMe 正文（Markdown）的请求体 */
public class AboutRequest {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
