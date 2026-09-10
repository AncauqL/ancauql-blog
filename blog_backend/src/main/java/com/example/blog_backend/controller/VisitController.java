package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.VisitRequest;
import com.example.blog_backend.service.IVisitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 访问统计接口。
 * POST /visit 公开（前台每次路由切换上报一次，不需要登录）；
 * GET /visit/dashboard 需管理员（见 AuthInterceptor）。
 */
@RestController
@RequestMapping("/visit")
public class VisitController {

    @Autowired
    private IVisitService visitService;

    @PostMapping
    public Result record(@RequestBody(required = false) VisitRequest body,
                         HttpServletRequest request) {
        // 管理员自己浏览不计入统计（与文章阅读量口径一致）
        if (AuthContext.isManager()) {
            return Result.success();
        }
        VisitRequest payload = body == null ? new VisitRequest() : body;
        visitService.record(payload.getPath(), payload.getArticleId(),
                clientIp(request), request.getHeader("User-Agent"));
        return Result.success();
    }

    @GetMapping("/dashboard")
    public Result dashboard(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(visitService.dashboard(days));
    }

    /** nginx 反代时真实 IP 在 X-Forwarded-For 的第一段 */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded)
                    .trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
