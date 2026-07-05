package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.entity.User;
import com.example.blog_backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private IUserService userService;

    // 查询全部
    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(userService.selectAll());
    }

    // 模糊搜索
    @GetMapping("/selectSearch")
    public Result selectSearch(@RequestParam String username) {
        return
                Result.success(userService.selectSearch(username));
    }

    // 分页查询
    @GetMapping("/selectPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String username) {
        return Result.success(userService.selectPage(pageNum,
                pageSize, username));
    }

    // 新增 / 编辑（id为null则新增，有id则更新）
    @PostMapping
    public Result insert(@RequestBody User user) {
        try {
            if (user.getId() == null) {
                userService.insert(user);
            } else {
                if (AuthContext.getUser() != null &&
                        user.getId().equals(AuthContext.getUser().getId()) &&
                        user.getRole() != null &&
                        !user.getRole().equals(AuthContext.getUser().getRole())) {
                    return Result.error("不能修改当前登录账号的角色");
                }
                userService.update(user);
            }
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        if (AuthContext.getUser() != null &&
                id.equals(AuthContext.getUser().getId())) {
            return Result.error("不能删除当前登录账号");
        }
        userService.delete(id);
        return Result.success();
    }
}
