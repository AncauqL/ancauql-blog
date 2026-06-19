package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.entity.Category;
import java.util.List;

public interface ICategoryService {
    List<Category> selectAll();
    List<Category> selectSearch(String categoryName);
    IPage<Category> selectPage(Integer pageNum, Integer pageSize,
                           String categoryName);
    void insert(Category category);
    void update(Category category);
    void delete(Integer id);
}