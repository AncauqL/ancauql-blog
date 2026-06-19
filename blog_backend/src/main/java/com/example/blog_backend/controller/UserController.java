package com.example.blog_backend.controller;

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
    public Result selectSearch(@RequestParam String userName) {
        return
                Result.success(userService.selectSearch(userName));
    }

    // 分页查询
    @GetMapping("/selectPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String userName) {
        return Result.success(userService.selectPage(pageNum,
                pageSize, userName));
    }

    // 新增 / 编辑（id为null则新增，有id则更新）
    @PostMapping
    public Result insert(@RequestBody User user) {
        if (user.getId() == null) {
            userService.insert(user);
        } else {
            userService.update(user);
        }
        return Result.success();
    }

    // 删除
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        userService.delete(id);
        return Result.success();
    }
}
