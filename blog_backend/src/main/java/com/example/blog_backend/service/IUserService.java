package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.entity.User;
import java.util.List;

public interface IUserService {
    List<User> selectAll();
    List<User> selectSearch(String userName);
    IPage<User> selectPage(Integer pageNum, Integer pageSize,
                           String userName);
    void insert(User user);
    void update(User user);
    void delete(Integer id);
}