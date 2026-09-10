package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.LoginRequest;
import com.example.blog_backend.dto.LoginResponse;
import com.example.blog_backend.dto.ProfileUpdateRequest;
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

    /** 登录失败计数与锁定：key -> [首次失败时间, 失败次数, 锁定到] */
    private static final ConcurrentHashMap<String, long[]> LOGIN_FAILS =
            new ConcurrentHashMap<>();
    private static final int LOGIN_MAX_FAILS = 5;
    private static final long LOGIN_WINDOW_MS = 15 * 60_000L;
    private static final long LOGIN_LOCK_MS = 15 * 60_000L;

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

    // 当前登录用户：绑定/更换邮箱、修改密码（需验当前密码）
    @PostMapping("/profile")
    public Result updateProfile(@RequestBody ProfileUpdateRequest body) {
        UserProfile self = AuthContext.getUser();
        if (self == null || self.getId() == null) {
            return Result.unauthorized();
        }
        try {
            User updated = userService.updateSelf(self.getId(),
                    body.getEmail(), body.getCurrentPassword(),
                    body.getNewPassword());
            return Result.success(UserProfile.from(updated));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result login(HttpServletRequest httpRequest,
                        @RequestBody LoginRequest request) {
        if (request.getUsername() == null ||
                request.getUsername().trim().isEmpty() ||
                request.getPassword() == null ||
                request.getPassword().trim().isEmpty()) {
            return Result.error("请输入账号和密码");
        }

        String ipKey = "ip:" + httpRequest.getRemoteAddr();
        String userKey = "user:" + request.getUsername().trim().toLowerCase();
        if (isLoginLocked(ipKey) || isLoginLocked(userKey)) {
            return Result.error("尝试过于频繁，账号已临时锁定，请约 15 分钟后再试");
        }

        User user = userService.login(request.getUsername(),
                request.getPassword());
        if (user == null) {
            recordLoginFail(ipKey);
            recordLoginFail(userKey);
            return Result.error(Result.UNAUTHORIZED, "账号或密码错误");
        }

        clearLoginFail(ipKey);
        clearLoginFail(userKey);
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

    private static boolean isLoginLocked(String key) {
        long[] bucket = LOGIN_FAILS.get(key);
        if (bucket == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (bucket[2] > now) {
            return true;
        }
        if (bucket[2] != 0) {
            // 锁定已过期，重新计数
            LOGIN_FAILS.remove(key);
        }
        return false;
    }

    private static void recordLoginFail(String key) {
        synchronized (LOGIN_FAILS) {
            long now = System.currentTimeMillis();
            long[] bucket = LOGIN_FAILS.get(key);
            if (bucket == null || now - bucket[0] >= LOGIN_WINDOW_MS) {
                LOGIN_FAILS.put(key, new long[]{now, 1, 0});
                return;
            }
            bucket[1]++;
            if (bucket[1] >= LOGIN_MAX_FAILS) {
                bucket[2] = now + LOGIN_LOCK_MS;
            }
        }
    }

    private static void clearLoginFail(String key) {
        LOGIN_FAILS.remove(key);
    }
}
