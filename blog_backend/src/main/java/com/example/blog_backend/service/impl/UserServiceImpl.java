package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import
        com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog_backend.common.PasswordUtil;
import com.example.blog_backend.common.RoleUtil;
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
        List<User> users = userMapper.selectList(null);
        users.forEach(this::hidePassword);
        return users;
    }

    @Override
    public User selectById(Integer id) {
        User user = userMapper.selectById(id);
        hidePassword(user);
        return user;
    }

    @Override
    public User selectByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public List<User> selectSearch(String username) {
        LambdaQueryWrapper<User> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(username) && username != null,
                User::getUsername, username);
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(this::hidePassword);
        return users;
    }

    @Override
    public IPage<User> selectPage(Integer pageNum, Integer
            pageSize, String username) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new
                LambdaQueryWrapper<>();
        wrapper.like(!"".equals(username) && username != null,
                User::getUsername, username);
        IPage<User> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::hidePassword);
        return result;
    }

    @Override
    public void insert(User user) {
        validateUser(user, true);
        if (selectByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("账号已存在");
        }
        user.setRole(normalizeRole(user));
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    public void update(User user) {
        validateUser(user, false);
        User old = userMapper.selectById(user.getId());
        if (old == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        User sameUsername = selectByUsername(user.getUsername());
        if (sameUsername != null &&
                !sameUsername.getId().equals(user.getId())) {
            throw new IllegalArgumentException("账号已存在");
        }
        user.setRole(normalizeRole(user));
        if (user.getPassword() == null ||
                user.getPassword().trim().isEmpty()) {
            user.setPassword(old.getPassword());
        } else {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        userMapper.updateById(user);
    }

    @Override
    public void delete(Integer id) {
        userMapper.deleteById(id);
    }

    @Override
    public User login(String username, String password) {
        User user = selectByUsername(username);
        if (user == null || !PasswordUtil.matches(password,
                user.getPassword())) {
            return null;
        }

        user.setRole(RoleUtil.normalize(user));
        if (!PasswordUtil.isEncoded(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(password));
            userMapper.updateById(user);
        }

        hidePassword(user);
        return user;
    }

    private void validateUser(User user, boolean requirePassword) {
        if (user.getUsername() == null ||
                user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("请输入账号");
        }
        if (requirePassword && (user.getPassword() == null ||
                user.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("请输入密码");
        }
    }

    private String normalizeRole(User user) {
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            user.setRole("ADMIN");
        }
        return RoleUtil.normalize(user);
    }

    private void hidePassword(User user) {
        if (user != null) {
            user.setPassword(null);
            user.setRole(RoleUtil.normalize(user));
        }
    }
}
