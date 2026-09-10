package com.example.blog_backend.dto;

/** 热门文章（按访问量排序） */
public class TopArticle {

    private Integer articleId;

    private String title;

    private Long pv;

    public TopArticle() {
    }

    public TopArticle(Integer articleId, String title, Long pv) {
        this.articleId = articleId;
        this.title = title;
        this.pv = pv;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPv() {
        return pv;
    }

    public void setPv(Long pv) {
        this.pv = pv;
    }
}
