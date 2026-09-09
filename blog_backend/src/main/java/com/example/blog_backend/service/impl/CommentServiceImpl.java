package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog_backend.entity.Comment;
import com.example.blog_backend.mapper.CommentMapper;
import com.example.blog_backend.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<Comment> selectByArticle(Integer articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId);
        wrapper.orderByAsc(Comment::getCreateTime);
        wrapper.orderByAsc(Comment::getId);
        return commentMapper.selectList(wrapper);
    }

    @Override
    public void insert(Comment comment) {
        commentMapper.insert(comment);
    }

    @Override
    public List<Comment> selectAll() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        wrapper.orderByDesc(Comment::getId);
        return commentMapper.selectList(wrapper);
    }

    @Override
    public void delete(Integer id) {
        commentMapper.deleteById(id);
    }
}
