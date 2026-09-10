package com.example.blog_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 文章评论（自建轻量评论，即发即显）。
 */
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer articleId;

    /** 发表评论的用户（旧游客评论可为空；新评论必为已登录用户） */
    private Integer userId;

    private String nickname;

    private String content;

    /** 所属顶层评论 id（NULL=顶层评论）；回复也统一挂在顶层下，只做两级展示 */
    private Integer parentId;

    /** 被回复者昵称（冗余存储：对方评论被删也能正常显示「回复 @某某」） */
    private String replyToNickname;

    private Integer likeCount;

    private LocalDateTime createTime;

    /** 当前访客是否已点赞（不落库，查询时回填） */
    @TableField(exist = false)
    private Boolean liked;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getReplyToNickname() {
        return replyToNickname;
    }

    public void setReplyToNickname(String replyToNickname) {
        this.replyToNickname = replyToNickname;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
