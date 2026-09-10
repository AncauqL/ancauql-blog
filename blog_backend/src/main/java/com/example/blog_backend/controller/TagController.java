package com.example.blog_backend.controller;

import com.example.blog_backend.common.Result;
import com.example.blog_backend.entity.Tag;
import com.example.blog_backend.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 标签接口。
 * GET /tag/selectAll 公开；POST /tag、DELETE /tag/delete 管理员（见 AuthInterceptor）。
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private ITagService tagService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(tagService.selectAllWithCount());
    }

    @PostMapping
    public Result save(@RequestBody Tag tag) {
        try {
            return Result.success(tagService.save(tag));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        tagService.delete(id);
        return Result.success();
    }
}
