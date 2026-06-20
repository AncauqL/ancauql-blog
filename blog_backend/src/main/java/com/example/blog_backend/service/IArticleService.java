package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.entity.Article;
import java.util.List;

public interface IArticleService {
    List<Article> selectAll();
    Article selectById(Integer id);
    List<Article> selectSearch(String articleTitle);
    IPage<Article> selectPage(Integer pageNum, Integer pageSize,
                           String articleTitle);
    void insert(Article article);
    void update(Article article);
    void delete(Integer id);
}
