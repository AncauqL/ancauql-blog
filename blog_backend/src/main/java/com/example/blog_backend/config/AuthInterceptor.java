package com.example.blog_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.UserProfile;
import com.example.blog_backend.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthTokenService authTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        UserProfile user = authTokenService.resolve(extractToken(request));
        AuthContext.setUser(user);

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (requiresSuperAdmin(path)) {
            if (!AuthContext.isLoggedIn()) {
                writeResult(response, Result.unauthorized());
                AuthContext.clear();
                return false;
            }
            if (!AuthContext.isSuperAdmin()) {
                writeResult(response, Result.forbidden());
                AuthContext.clear();
                return false;
            }
            return true;
        }

        if (requiresManager(path, method)) {
            if (!AuthContext.isLoggedIn()) {
                writeResult(response, Result.unauthorized());
                AuthContext.clear();
                return false;
            }
            if (!AuthContext.isManager()) {
                writeResult(response, Result.forbidden());
                AuthContext.clear();
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        AuthContext.clear();
    }

    private boolean requiresSuperAdmin(String path) {
        return path.startsWith("/user");
    }

    private boolean requiresManager(String path, String method) {
        if (path.equals("/auth/me") || path.equals("/auth/logout")) {
            return true;
        }

        // 图片上传等文件操作全部需要管理员
        if (path.startsWith("/file")) {
            return true;
        }

        if (path.startsWith("/article")) {
            return !"GET".equalsIgnoreCase(method);
        }

        if (path.startsWith("/category")) {
            return !"GET".equalsIgnoreCase(method);
        }

        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null &&
                authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("token");
    }

    private void writeResult(HttpServletResponse response, Result result)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
