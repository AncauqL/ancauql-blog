package com.example.blog_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访问日志（原始记录）。
 * 隐私：只存 md5(IP + UA + 盐) 作为访客标识，不落原始 IP。
 */
@TableName("visit_log")
public class VisitLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访问日期（服务器时区），趋势统计按它分组 */
    private LocalDate statDate;

    private String path;

    /** 文章页才有值，用于「热门文章」 */
    private Integer articleId;

    private String visitorKey;

    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public String getVisitorKey() {
        return visitorKey;
    }

    public void setVisitorKey(String visitorKey) {
        this.visitorKey = visitorKey;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
