package com.example.blog_backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 评论点赞去重记录（复合主键 comment_id + visitor_key）。
 * 有了它，同一访客对同一条评论只能点一次赞，再次点击即为取消。
 */
@TableName("comment_like")
public class CommentLike {

    private Integer commentId;

    /** 已登录为 u{用户ID}，未登录为 md5(IP+UA+盐) */
    private String visitorKey;

    private LocalDateTime createTime;

    public CommentLike() {
    }

    public CommentLike(Integer commentId, String visitorKey) {
        this.commentId = commentId;
        this.visitorKey = visitorKey;
        this.createTime = LocalDateTime.now();
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
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
