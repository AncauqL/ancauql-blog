package com.example.blog_backend.dto;

/**
 * 首页统计：已发布文章数与总阅读量。
 */
public class SiteStats {

    private Long articleCount;
    private Long totalViews;

    public SiteStats() {
    }

    public SiteStats(Long articleCount, Long totalViews) {
        this.articleCount = articleCount;
        this.totalViews = totalViews;
    }

    public Long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }
}
