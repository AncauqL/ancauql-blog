package com.example.blog_backend.dto;

import java.time.LocalDate;

/** 每日访问量（趋势图用） */
public class DailyVisit {

    private LocalDate statDate;

    /** 页面浏览量 */
    private Long pv;

    /** 独立访客数（按 visitor_key 去重） */
    private Long uv;

    public DailyVisit() {
    }

    public DailyVisit(LocalDate statDate, Long pv, Long uv) {
        this.statDate = statDate;
        this.pv = pv;
        this.uv = uv;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Long getPv() {
        return pv;
    }

    public void setPv(Long pv) {
        this.pv = pv;
    }

    public Long getUv() {
        return uv;
    }

    public void setUv(Long uv) {
        this.uv = uv;
    }
}
