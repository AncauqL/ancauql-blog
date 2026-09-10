package com.example.blog_backend.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码哈希工具。
 * 现在统一用 BCrypt（`BCRYPT:` 前缀，自带随机盐）；同时兼容历史数据：
 * - `SHA256:` 前缀：旧的单次 SHA-256（无盐），登录成功后会升级成 BCrypt；
 * - 无前缀：更早的明文密码，登录成功后同样升级。
 */
public class PasswordUtil {

    private static final String BCRYPT_PREFIX = "BCRYPT:";
    private static final String SHA256_PREFIX = "SHA256:";
    private static final BCryptPasswordEncoder ENCODER =
            new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 生成存储值（BCrypt，含随机盐） */
    public static String encode(String rawPassword) {
        return BCRYPT_PREFIX + ENCODER.encode(
                rawPassword == null ? "" : rawPassword);
    }

    /** 校验明文密码与存储值（兼容 BCrypt / SHA256 / 明文） */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        String raw = rawPassword == null ? "" : rawPassword;

        if (storedPassword.startsWith(BCRYPT_PREFIX)) {
            return ENCODER.matches(raw,
                    storedPassword.substring(BCRYPT_PREFIX.length()));
        }
        if (storedPassword.startsWith(SHA256_PREFIX)) {
            return (SHA256_PREFIX + sha256(raw)).equals(storedPassword);
        }
        // 兼容旧数据库中的明文密码
        return storedPassword.equals(rawPassword);
    }

    /** 是否需要在登录成功后就地升级为 BCrypt */
    public static boolean needsRehash(String storedPassword) {
        return storedPassword == null
                || !storedPassword.startsWith(BCRYPT_PREFIX);
    }

    public static boolean isEncoded(String storedPassword) {
        return storedPassword != null
                && (storedPassword.startsWith(BCRYPT_PREFIX)
                || storedPassword.startsWith(SHA256_PREFIX));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8));
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
