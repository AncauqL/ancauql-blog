package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.LoginRequest;
import com.example.blog_backend.dto.LoginResponse;
import com.example.blog_backend.dto.UserProfile;
import com.example.blog_backend.entity.User;
import com.example.blog_backend.service.AuthTokenService;
import com.example.blog_backend.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

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
}
