package com.example.blog_backend.mapper;

import com.example.blog_backend.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category>{

    // 查询所有
    List<Category> selectAll();

    // 批量添加
    void batchInsert(List<Category> list);

    //自定义update
    void update(Category user);


}
