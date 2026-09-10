package com.example.blog_backend.dto;

/**
 * 发表评论的请求体。
 * website 为“蜜罐”字段：真实用户看不到，只有爬虫/灌水机器人会填，
 * 命中即静默忽略，不落库。
 */
public class CommentAddRequest {

    private Integer articleId;

    private String nickname;

    private String content;

    /** 回复某条评论时带上级评论 id；为空表示发顶层评论 */
    private Integer parentId;

    private String website;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
