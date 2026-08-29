package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.dto.ArticleNeighbors;
import com.example.blog_backend.entity.Article;
import java.util.List;

public interface IArticleService {
    List<Article> selectAll();
    List<Article> selectPublishedAll();
    Article selectById(Integer id);

    /** 阅读量 +1（数据库原子自增，避免并发丢计数） */
    void increaseViewCount(Integer id);

    /** 查询同为已发布状态的上一篇 / 下一篇 */
    ArticleNeighbors selectNeighbors(Integer id);

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
