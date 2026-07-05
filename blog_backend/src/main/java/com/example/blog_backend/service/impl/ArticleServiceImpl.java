package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import
        com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.mapper.ArticleMapper;
import com.example.blog_backend.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            pageSize, String articleTitle) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(articleTitle) && articleTitle != null,
                Article::getTitle, articleTitle);
        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Article> selectPublishedPage(Integer pageNum, Integer
            pageSize, String articleTitle) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, "published");
        wrapper.like(!"".equals(articleTitle) && articleTitle != null,
                Article::getTitle, articleTitle);
        return articleMapper.selectPage(page, wrapper);
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
