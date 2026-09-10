package com.example.blog_backend.dto;

/** 点赞请求体 */
public class CommentLikeRequest {

    private Integer commentId;

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }
}
