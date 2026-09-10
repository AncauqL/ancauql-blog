package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.dto.ArchiveGroup;
import com.example.blog_backend.dto.ArticleNeighbors;
import com.example.blog_backend.dto.SiteStats;
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

    /** 归档：已发布文章按年份分组（年份与组内文章均倒序） */
    List<ArchiveGroup> selectArchive();

    /** 站点统计：已发布文章数 + 总阅读量 */
    SiteStats selectStats();

    /**
     * 管理端分页：可按标题模糊、状态精确、分类/标签过滤。
     * 按置顶优先、创建时间倒序；列表不返回 content 字段。
     */
    IPage<Article> selectPage(Integer pageNum, Integer pageSize,
                           String articleTitle, String status,
                           Integer categoryId, Integer tagId);

    /**
     * 访客分页：只返回已发布文章，其余同 selectPage。
     */
    IPage<Article> selectPublishedPage(Integer pageNum, Integer pageSize,
                           String articleTitle, Integer categoryId,
                           Integer tagId);
    /**
     * 站内搜索分页：关键词同时匹配标题 / 摘要 / 正文，按创建时间倒序。
     * 列表不返回 content 字段；onlyPublished=true 时只搜已发布文章（访客视角）。
     */
    IPage<Article> selectSearchPage(Integer pageNum, Integer pageSize,
                                    String keyword, boolean onlyPublished);

    void insert(Article article);
    void update(Article article);
    void delete(Integer id);
}
