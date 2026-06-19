package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import
        com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog_backend.entity.Category;
import com.example.blog_backend.mapper.CategoryMapper;
import com.example.blog_backend.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> selectAll() {
        return categoryMapper.selectList(null);
    }

    @Override
    public List<Category> selectSearch(String categoryName) {
        LambdaQueryWrapper<Category> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(categoryName) && categoryName != null,
                Category::getName, categoryName);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public IPage<Category> selectPage(Integer pageNum, Integer
            pageSize, String categoryName) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(categoryName) && categoryName != null,
                Category::getName, categoryName);
        return categoryMapper.selectPage(page, wrapper);
    }

    @Override
    public void insert(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.deleteById(id);
    }
}
