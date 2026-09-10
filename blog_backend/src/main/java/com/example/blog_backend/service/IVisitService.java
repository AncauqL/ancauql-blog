package com.example.blog_backend.service;

import com.example.blog_backend.dto.VisitDashboard;

public interface IVisitService {

    /**
     * 记录一次页面访问。
     * 管理员自己浏览不计数；明显是爬虫的 UA 不计数；同一访客对同一路径 3 秒内重复上报只算一次。
     *
     * @param path      访问路径（会截断到 200 字）
     * @param articleId 文章页的文章 ID，可为空
     * @param ip        客户端 IP（用于生成访客标识，不落库）
     * @param userAgent 客户端 UA（用于生成访客标识与爬虫过滤）
     */
    void record(String path, Integer articleId, String ip, String userAgent);

    /** 统计仪表盘数据：总览 + 最近 days 天趋势 + 热门文章 */
    VisitDashboard dashboard(Integer days);
}
