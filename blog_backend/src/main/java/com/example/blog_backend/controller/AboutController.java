package com.example.blog_backend.controller;

import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.AboutRequest;
import com.example.blog_backend.service.IAboutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AboutMe 正文（Markdown）。
 * GET /about 公开读取；PUT /about 管理员保存。
 */
@RestController
@RequestMapping("/about")
@CrossOrigin(origins = "*")
public class AboutController {

    @Autowired
    private IAboutService aboutService;

    @GetMapping
    public Result get() {
        return Result.success(aboutService.getContent());
    }

    @PutMapping
    public Result save(@RequestBody AboutRequest request) {
        String content = request.getContent() == null ? "" : request.getContent();
        aboutService.saveContent(content);
        return Result.success();
    }
}
