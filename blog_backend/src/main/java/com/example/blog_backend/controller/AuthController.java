package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.LoginRequest;
import com.example.blog_backend.dto.LoginResponse;
import com.example.blog_backend.dto.RegisterRequest;
import com.example.blog_backend.dto.UserProfile;
import com.example.blog_backend.entity.User;
import com.example.blog_backend.service.AuthTokenService;
import com.example.blog_backend.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final IUserService userService;
    private final AuthTokenService authTokenService;

    public AuthController(IUserService userService,
                          AuthTokenService authTokenService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
    }

    private static final ConcurrentHashMap<String, long[]> REG_LIMITER =
            new ConcurrentHashMap<>();
    private static final long REG_WINDOW_MS = 60_000L;
    private static final int REG_MAX = 5;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    // 开放注册：只创建普通用户(USER)，邮箱即账号名；注册成功直接登录
    @PostMapping("/register")
    public Result register(HttpServletRequest request,
                           @RequestBody RegisterRequest body) {
        // 蜜罐命中：静默丢弃（灌水脚本看不见这个字段）
        if (body.getWebsite() != null && !body.getWebsite().trim().isEmpty()) {
            return Result.success();
        }
        if (!allowRegister(request.getRemoteAddr())) {
            return Result.error("注册太频繁，请稍后再试");
        }
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        if (!email.matches(EMAIL_PATTERN)) {
            return Result.error("邮箱格式不正确");
        }
        try {
            User user = userService.register(email, body.getNickname(),
                    body.getPassword());
            UserProfile profile = UserProfile.from(user);
            String token = authTokenService.createToken(profile);
            return Result.success(new LoginResponse(token, profile));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null ||
                request.getUsername().trim().isEmpty() ||
                request.getPassword() == null ||
                request.getPassword().trim().isEmpty()) {
            return Result.error("请输入账号和密码");
        }

        User user = userService.login(request.getUsername(),
                request.getPassword());
        if (user == null) {
            return Result.error(Result.UNAUTHORIZED, "账号或密码错误");
        }

        UserProfile profile = UserProfile.from(user);
        String token = authTokenService.createToken(profile);
        return Result.success(new LoginResponse(token, profile));
    }

    @GetMapping("/me")
    public Result me() {
        return Result.success(AuthContext.getUser());
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        authTokenService.invalidate(extractToken(request));
        return Result.success();
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null &&
                authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("token");
    }

    private static boolean allowRegister(String ip) {
        String key = ip == null || ip.isEmpty() ? "unknown" : ip;
        long now = System.currentTimeMillis();
        synchronized (REG_LIMITER) {
            long[] bucket = REG_LIMITER.get(key);
            if (bucket == null || now - bucket[0] >= REG_WINDOW_MS) {
                REG_LIMITER.put(key, new long[]{now, 1});
                return true;
            }
            if (bucket[1] >= REG_MAX) {
                return false;
            }
            bucket[1]++;
            return true;
        }
    }
}
