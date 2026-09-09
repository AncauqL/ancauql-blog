package com.example.blog_backend.common;

import com.example.blog_backend.entity.User;

public class RoleUtil {
    private RoleUtil() {
    }

    public static String normalize(User user) {
        if (user == null) {
            return "GUEST";
        }

        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            return "ADMIN";
        }

        String normalized = role.trim().toUpperCase();
        if ("SUPER".equals(normalized) || "OWNER".equals(normalized) ||
                "SUPERADMIN".equals(normalized)) {
            return "SUPER_ADMIN";
        }

        if ("ADMIN".equals(normalized) &&
                "admin".equalsIgnoreCase(user.getUsername())) {
            return "SUPER_ADMIN";
        }

        if ("USER".equals(normalized)) {
            return "USER";
        }

        if ("ADMIN".equals(normalized) ||
                "SUPER_ADMIN".equals(normalized)) {
            return normalized;
        }

        // 其它未知角色一律保守归为普通用户，绝不默认升级成管理员
        return "USER";
    }
}
