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

        // /auth/me、/auth/logout、/auth/profile：任意已登录用户即可
        if (path.equals("/auth/me") || path.equals("/auth/logout")
                || path.equals("/auth/profile")) {
            if (!AuthContext.isLoggedIn()) {
                writeResult(response, Result.unauthorized());
                AuthContext.clear();
                return false;
            }
            return true;
        }

        // 评论：读某文章评论公开；发表/删除需登录（任意角色，controller 再校验本人）；
        // 管理端列表仅管理员
        if (path.startsWith("/comment")) {
            boolean isList = "/comment/list".equals(path);
            boolean isGet = "GET".equalsIgnoreCase(method);
            if (!isGet || isList) {
                if (!AuthContext.isLoggedIn()) {
                    writeResult(response, Result.unauthorized());
                    AuthContext.clear();
                    return false;
                }
                if (isList && !AuthContext.isManager()) {
                    writeResult(response, Result.forbidden());
                    AuthContext.clear();
                    return false;
                }
            }
            return true;
        }

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
        // 图片上传等文件操作全部需要管理员
        if (path.startsWith("/file")) {
            return true;
        }

        // AboutMe：公开读，写操作需管理员
        if (path.startsWith("/about")
                && !"GET".equalsIgnoreCase(method)) {
            return true;
        }

        if (path.startsWith("/article")) {
            return !"GET".equalsIgnoreCase(method);
        }

        if (path.startsWith("/category")) {
            return !"GET".equalsIgnoreCase(method);
        }

        // 标签：公开读（列表/筛选），新建与删除需管理员
        if (path.startsWith("/tag")) {
            return !"GET".equalsIgnoreCase(method);
        }

        // 站点信息：公开读（前台启动时拉取），保存需管理员
        if (path.startsWith("/site")) {
            return !"GET".equalsIgnoreCase(method);
        }

        // 访问统计：上报（POST）公开，统计看板（GET）需管理员
        if (path.startsWith("/visit")) {
            return "GET".equalsIgnoreCase(method);
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
