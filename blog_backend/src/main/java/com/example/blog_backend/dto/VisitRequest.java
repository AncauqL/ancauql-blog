package com.example.blog_backend.dto;

/** 前台页面上报的访问事件 */
public class VisitRequest {

    /** 访问路径，如 /、/post/12、/archive?tag=1 */
    private String path;

    /** 文章页带上文章 ID，其余页面为空 */
    private Integer articleId;

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
}
