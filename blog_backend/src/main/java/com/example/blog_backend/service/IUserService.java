package com.example.blog_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog_backend.entity.User;
import java.util.List;

public interface IUserService {
    List<User> selectAll();
    User selectById(Integer id);
    User selectByUsername(String username);
    List<User> selectSearch(String username);
    IPage<User> selectPage(Integer pageNum, Integer pageSize,
                           String username);
    void insert(User user);
    void update(User user);
    void delete(Integer id);
    User login(String username, String password);
}
