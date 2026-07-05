package com.example.blog_backend.common;

import com.example.blog_backend.dto.UserProfile;

public class AuthContext {
    private static final ThreadLocal<UserProfile> USER_HOLDER =
            new ThreadLocal<>();

    public static void setUser(UserProfile user) {
        USER_HOLDER.set(user);
    }

    public static UserProfile getUser() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    public static boolean isLoggedIn() {
        return getUser() != null;
    }

    public static boolean isManager() {
        UserProfile user = getUser();
        if (user == null || user.getRole() == null) {
            return false;
        }
        return "SUPER_ADMIN".equals(user.getRole()) ||
                "ADMIN".equals(user.getRole());
    }

    public static boolean isSuperAdmin() {
        UserProfile user = getUser();
        return user != null && "SUPER_ADMIN".equals(user.getRole());
    }
}
