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

    /**
     * 开放注册（仅普通用户）：邮箱=账号名，角色强制 USER。
     * 邮箱重复等会抛 IllegalArgumentException。
     */
    User register(String email, String nickname, String password);

    /**
     * 当前登录用户更新自己的资料（绑定/更换邮箱、修改密码）。
     * 需校验当前密码；邮箱需全站唯一；异常抛 IllegalArgumentException。
     */
    User updateSelf(Integer id, String email, String currentPassword,
                    String newPassword);
}
