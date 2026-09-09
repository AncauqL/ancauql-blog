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

    private User selectByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email.trim());
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
    public User login(String identifier, String password) {
        User user = selectByUsername(identifier);
        if (user == null) {
            // 兼容“用邮箱登录”（如 admin 绑定邮箱后可用邮箱登录）
            user = selectByEmail(identifier);
        }
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

    @Override
    public User register(String email, String nickname, String password) {
        String account = email == null ? "" : email.trim();
        String nick = nickname == null ? "" : nickname.trim();
        if (account.isEmpty()) {
            throw new IllegalArgumentException("请输入邮箱");
        }
        if (nick.isEmpty()) {
            throw new IllegalArgumentException("请输入昵称");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("密码至少 8 位");
        }
        if (selectByUsername(account) != null) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }

        User user = new User();
        user.setUsername(account);
        user.setEmail(account);
        user.setNickname(nick);
        user.setRole("USER");
        user.setPassword(PasswordUtil.encode(password));
        userMapper.insert(user);
        hidePassword(user);
        return user;
    }

    @Override
    public User updateSelf(Integer id, String email, String currentPassword,
                          String newPassword) {
        User old = userMapper.selectById(id);
        if (old == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (currentPassword == null
                || !PasswordUtil.matches(currentPassword, old.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }

        boolean changed = false;
        String newEmail = email == null ? null : email.trim();
        if (newEmail != null && !newEmail.isEmpty()) {
            String currentEmail = old.getEmail() == null
                    ? "" : old.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(currentEmail)) {
                if (isEmailTakenByOther(newEmail, id)) {
                    throw new IllegalArgumentException("该邮箱已被使用");
                }
                old.setEmail(newEmail);
                changed = true;
            }
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 8) {
                throw new IllegalArgumentException("新密码至少 8 位");
            }
            old.setPassword(PasswordUtil.encode(newPassword));
            changed = true;
        }
        if (!changed) {
            throw new IllegalArgumentException("没有需要修改的内容");
        }

        userMapper.updateById(old);
        hidePassword(old);
        return old;
    }

    private boolean isEmailTakenByOther(String email, Integer selfId) {
        LambdaQueryWrapper<User> byUsername = new LambdaQueryWrapper<>();
        byUsername.eq(User::getUsername, email);
        Long u = userMapper.selectCount(byUsername);
        if (u != null && u > 0) {
            return true;
        }
        LambdaQueryWrapper<User> byEmail = new LambdaQueryWrapper<>();
        byEmail.eq(User::getEmail, email);
        byEmail.ne(User::getId, selfId);
        Long e = userMapper.selectCount(byEmail);
        return e != null && e > 0;
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
