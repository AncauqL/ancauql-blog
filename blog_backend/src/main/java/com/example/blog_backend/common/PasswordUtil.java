package com.example.blog_backend.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    private static final String PREFIX = "SHA256:";

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        return PREFIX + sha256(rawPassword == null ? "" : rawPassword);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith(PREFIX)) {
            return encode(rawPassword).equals(storedPassword);
        }

        // 兼容旧数据库中的明文密码，登录成功后会升级为哈希。
        return storedPassword.equals(rawPassword);
    }

    public static boolean isEncoded(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PREFIX);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : encoded) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
