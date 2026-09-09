package com.example.blog_backend.service;

import com.example.blog_backend.entity.Comment;

import java.util.List;

public interface ICommentService {

    /** 某篇文章的评论（按时间升序，正文顺序） */
    List<Comment> selectByArticle(Integer articleId);

    /** 新增一条评论 */
    void insert(Comment comment);

    /** 管理端：全部评论（新的在前） */
    List<Comment> selectAll();

    /** 删除一条评论 */
    void delete(Integer id);
}
