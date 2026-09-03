package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import
        com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog_backend.dto.ArchiveGroup;
import com.example.blog_backend.dto.ArticleNeighbors;
import com.example.blog_backend.dto.SiteStats;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.mapper.ArticleMapper;
import com.example.blog_backend.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements IArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<Article> selectAll() {
        return articleMapper.selectList(null);
    }

    @Override
    public List<Article> selectPublishedAll() {
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, "published");
        return articleMapper.selectList(wrapper);
    }

    @Override
    public Article selectById(Integer id) {
        return articleMapper.selectById(id);
    }

    @Override
    public void increaseViewCount(Integer id) {
        UpdateWrapper<Article> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        // 数据库端原子自增，兼容历史数据中 view_count 为 NULL 的情况
        wrapper.setSql("view_count = IFNULL(view_count, 0) + 1");
        articleMapper.update(null, wrapper);
    }

    @Override
    public ArticleNeighbors selectNeighbors(Integer id) {
        // 只取导航需要的最小字段，按发布时间 + id 升序排出全站阅读顺序
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.select(Article::getId, Article::getTitle,
                Article::getCreateTime);
        wrapper.eq(Article::getStatus, "published");
        wrapper.orderByAsc(Article::getCreateTime);
        wrapper.orderByAsc(Article::getId);
        List<Article> articles = articleMapper.selectList(wrapper);

        int index = -1;
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }

        ArticleNeighbors neighbors = new ArticleNeighbors();
        if (index < 0) {
            // 当前文章不在已发布列表中（例如草稿预览），不提供导航
            return neighbors;
        }
        if (index > 0) {
            Article prev = articles.get(index - 1);
            neighbors.setPrev(new ArticleNeighbors.ArticleBrief(
                    prev.getId(), prev.getTitle()));
        }
        if (index < articles.size() - 1) {
            Article next = articles.get(index + 1);
            neighbors.setNext(new ArticleNeighbors.ArticleBrief(
                    next.getId(), next.getTitle()));
        }
        return neighbors;
    }

    @Override
    public List<Article> selectSearch(String articleTitle) {
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(articleTitle) && articleTitle != null,
                Article::getTitle, articleTitle);
        return articleMapper.selectList(wrapper);
    }

    @Override
    public List<Article> selectPublishedSearch(String articleTitle) {
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, "published");
        wrapper.like(!"".equals(articleTitle) && articleTitle != null,
                Article::getTitle, articleTitle);
        return articleMapper.selectList(wrapper);
    }

    @Override
    public IPage<Article> selectPage(Integer pageNum, Integer
            pageSize, String articleTitle, String status,
            Integer categoryId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper =
                listWrapper(articleTitle, categoryId);
        wrapper.eq(status != null && !"".equals(status),
                Article::getStatus, status);
        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Article> selectPublishedPage(Integer pageNum, Integer
            pageSize, String articleTitle, Integer categoryId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper =
                listWrapper(articleTitle, categoryId);
        wrapper.eq(Article::getStatus, "published");
        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<ArchiveGroup> selectArchive() {
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.select(Article::getId, Article::getTitle,
                Article::getCreateTime);
        wrapper.eq(Article::getStatus, "published");
        wrapper.orderByDesc(Article::getCreateTime);
        wrapper.orderByDesc(Article::getId);
        List<Article> articles = articleMapper.selectList(wrapper);

        // 已按时间倒序，顺序装组即可保证年份倒序、组内倒序
        List<ArchiveGroup> groups = new ArrayList<>();
        ArchiveGroup current = null;
        for (Article article : articles) {
            int year = article.getCreateTime() == null
                    ? 0 : article.getCreateTime().getYear();
            if (current == null || !current.getYear().equals(year)) {
                current = new ArchiveGroup(year);
                groups.add(current);
            }
            current.getArticles().add(new ArchiveGroup.ArchiveItem(
                    article.getId(), article.getTitle(),
                    article.getCreateTime()));
        }
        return groups;
    }

    @Override
    public SiteStats selectStats() {
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.select(Article::getStatus, Article::getViewCount);
        wrapper.eq(Article::getStatus, "published");
        List<Article> articles = articleMapper.selectList(wrapper);

        long totalViews = 0;
        for (Article article : articles) {
            totalViews += article.getViewCount() == null
                    ? 0 : article.getViewCount();
        }
        return new SiteStats((long) articles.size(), totalViews);
    }

    /**
     * 列表查询公共条件：
     * - 排除 content 大字段（编辑 / 阅读时通过 detail 单独取正文）
     * - 标题模糊匹配（可选）、分类过滤（可选）
     * - 创建时间倒序，同时间按 id 倒序兜底
     */
    private LambdaQueryWrapper<Article> listWrapper(String articleTitle,
                                                    Integer categoryId) {
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.select(Article.class,
                info -> !"content".equals(info.getColumn()));
        wrapper.like(!"".equals(articleTitle) && articleTitle != null,
                Article::getTitle, articleTitle);
        wrapper.eq(categoryId != null, Article::getCategoryId, categoryId);
        // 置顶优先，再按创建时间倒序（同时间按 id 倒序兜底）
        wrapper.orderByDesc(Article::getTop);
        wrapper.orderByDesc(Article::getCreateTime);
        wrapper.orderByDesc(Article::getId);
        return wrapper;
    }

    @Override
    public void insert(Article article) {
        articleMapper.insert(article);
    }

    @Override
    public void update(Article article) {
        articleMapper.updateById(article);
    }

    @Override
    public void delete(Integer id) {
        articleMapper.deleteById(id);
    }
}
