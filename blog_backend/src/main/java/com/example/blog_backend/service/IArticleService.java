package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.entity.Article;
import java.util.List;

public interface IArticleService {
    List<Article> selectAll();
    List<Article> selectPublishedAll();
    Article selectById(Integer id);
    List<Article> selectSearch(String articleTitle);
    List<Article> selectPublishedSearch(String articleTitle);
    IPage<Article> selectPage(Integer pageNum, Integer pageSize,
                           String articleTitle);
    IPage<Article> selectPublishedPage(Integer pageNum, Integer pageSize,
                           String articleTitle);
    void insert(Article article);
    void update(Article article);
    void delete(Integer id);
}
