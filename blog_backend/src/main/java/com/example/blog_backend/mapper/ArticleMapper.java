package com.example.blog_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog_backend.entity.Article;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article>{

    // 查询所有
    List<Article> selectAll();

    // 批量添加
    void batchInsert(List<Article> list);

    //自定义update
    void update(Article user);


}
