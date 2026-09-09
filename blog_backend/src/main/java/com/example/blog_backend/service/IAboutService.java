package com.example.blog_backend.service;

public interface IAboutService {

    /** 取 AboutMe 正文 Markdown（无记录时返回空串） */
    String getContent();

    /** 保存 AboutMe 正文 Markdown（单行 upsert） */
    void saveContent(String content);
}
