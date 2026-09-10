package com.example.blog_backend.controller;

import com.example.blog_backend.common.Result;
import com.example.blog_backend.service.ISiteConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 站点信息接口。
 * GET /site 公开（前台启动时拉取，缺失的项前端回退代码默认值）；
 * PUT /site 需管理员（见 AuthInterceptor）。
 */
@RestController
@RequestMapping("/site")
public class SiteController {

    @Autowired
    private ISiteConfigService siteConfigService;

    @GetMapping
    public Result get() {
        return Result.success(siteConfigService.getConfig());
    }

    @PutMapping
    public Result save(@RequestBody(required = false)
                       Map<String, Object> config) {
        // 传 {} 即表示「清空后台配置，恢复代码默认值」
        return Result.success(siteConfigService.saveConfig(config));
    }
}
