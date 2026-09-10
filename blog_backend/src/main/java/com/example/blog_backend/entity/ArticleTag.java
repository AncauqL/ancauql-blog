package com.example.blog_backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/** 文章-标签关联（复合主键，无自增 id） */
@TableName("article_tag")
public class ArticleTag {

    private Integer articleId;

    private Integer tagId;

    public ArticleTag() {
    }

    public ArticleTag(Integer articleId, Integer tagId) {
        this.articleId = articleId;
        this.tagId = tagId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }
}
