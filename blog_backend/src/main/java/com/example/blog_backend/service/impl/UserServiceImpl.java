package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import
        com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog_backend.entity.User;
import com.example.blog_backend.mapper.UserMapper;
import com.example.blog_backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> selectAll() {
        return userMapper.selectList(null);
    }

    @Override
    public List<User> selectSearch(String userName) {
        LambdaQueryWrapper<User> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(userName) && userName != null,
                User::getUserName, userName);
        return userMapper.selectList(wrapper);
    }

    @Override
    public IPage<User> selectPage(Integer pageNum, Integer
            pageSize, String userName) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(userName) && userName != null,
                User::getUserName, userName);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public void insert(User user) {
        userMapper.insert(user);
    }

    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Override
    public void delete(Integer id) {
        userMapper.deleteById(id);
    }
}
