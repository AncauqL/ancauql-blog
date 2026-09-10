package com.example.blog_backend.service;

import com.example.blog_backend.entity.Comment;

import java.util.List;
import java.util.Map;

public interface ICommentService {

    /**
     * 某篇文章的评论（按时间升序），并把回复挂在所属顶层评论之后。
     *
     * @param visitorKey 当前访客标识，用于回填每条评论的 liked；
     *                   传 null 表示不关心点赞状态
     */
    List<Comment> selectByArticle(Integer articleId, String visitorKey);

    /** 按 id 查单条评论（用于删除前的权限校验） */
    Comment selectById(Integer id);

    /** 新增一条评论（顶层或回复均可，回复的 parentId 需已由调用方校验） */
    void insert(Comment comment);

    /** 管理端：全部评论（新的在前） */
    List<Comment> selectAll();

    /**
     * 删除一条评论：连带删除它的所有回复与相关点赞记录。
     *
     * @return 被删除的评论条数（含回复）
     */
    int delete(Integer id);

    /**
     * 点赞 / 取消点赞（同一访客对同一条评论只能点一次，再点即取消）。
     *
     * @return {likeCount: 最新点赞数, liked: 操作后是否已点赞}
     */
    Map<String, Object> toggleLike(Integer commentId, String visitorKey);
}
