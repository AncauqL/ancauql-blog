package com.example.blog_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog_backend.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}
