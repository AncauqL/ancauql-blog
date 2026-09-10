package com.example.blog_backend.dto;

import java.util.ArrayList;
import java.util.List;

/** 统计仪表盘一次返回：总览 + 每日趋势 + 热门文章 */
public class VisitDashboard {

    private Integer days = 30;

    private VisitOverview overview = new VisitOverview();

    private List<DailyVisit> daily = new ArrayList<>();

    private List<TopArticle> topArticles = new ArrayList<>();

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public VisitOverview getOverview() {
        return overview;
    }

    public void setOverview(VisitOverview overview) {
        this.overview = overview;
    }

    public List<DailyVisit> getDaily() {
        return daily;
    }

    public void setDaily(List<DailyVisit> daily) {
        this.daily = daily;
    }

    public List<TopArticle> getTopArticles() {
        return topArticles;
    }

    public void setTopArticles(List<TopArticle> topArticles) {
        this.topArticles = topArticles;
    }
}
