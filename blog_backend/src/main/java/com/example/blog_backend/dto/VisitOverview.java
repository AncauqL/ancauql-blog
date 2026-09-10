package com.example.blog_backend.dto;

/** 访问总览（仪表盘顶部卡片） */
public class VisitOverview {

    private Long todayPv = 0L;
    private Long todayUv = 0L;
    private Long rangePv = 0L;
    private Long rangeUv = 0L;
    private Long totalPv = 0L;
    private Long totalUv = 0L;

    public Long getTodayPv() {
        return todayPv;
    }

    public void setTodayPv(Long todayPv) {
        this.todayPv = todayPv;
    }

    public Long getTodayUv() {
        return todayUv;
    }

    public void setTodayUv(Long todayUv) {
        this.todayUv = todayUv;
    }

    public Long getRangePv() {
        return rangePv;
    }

    public void setRangePv(Long rangePv) {
        this.rangePv = rangePv;
    }

    public Long getRangeUv() {
        return rangeUv;
    }

    public void setRangeUv(Long rangeUv) {
        this.rangeUv = rangeUv;
    }

    public Long getTotalPv() {
        return totalPv;
    }

    public void setTotalPv(Long totalPv) {
        this.totalPv = totalPv;
    }

    public Long getTotalUv() {
        return totalUv;
    }

    public void setTotalUv(Long totalUv) {
        this.totalUv = totalUv;
    }
}
