package com.example.blog_backend.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 访客标识工具：用于访问统计与评论点赞去重。
 *
 * 规则：
 * - 已登录用户 → {@code u{用户ID}}：换设备也认得是同一个人；
 * - 未登录访客 → {@code md5(IP + UA + 盐)}：够去重，且**不保存原始 IP**。
 *
 * 盐只是让哈希不可反查，不是密钥。
 */
public final class VisitorKeyUtil {

    private static final String SALT = "ancauql-blog-visit";

    /** 未登录访客标识长度 32（md5 十六进制），登录用户为 u+id（远小于 48） */
    public static final int MAX_LENGTH = 48;

    private VisitorKeyUtil() {
    }

    public static String of(Integer userId, String ip, String userAgent) {
        if (userId != null) {
            return "u" + userId;
        }
        String raw = (ip == null ? "" : ip) + '|'
                + (userAgent == null ? "" : userAgent) + '|' + SALT;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            // 理论上不会发生；退化成 hashCode，保证不影响功能
            return String.format("%032x", raw.hashCode());
        }
    }

    /** 反代场景取真实客户端 IP：优先 X-Forwarded-For 第一段，其次 X-Real-IP */
    public static String clientIp(String forwardedFor, String realIp,
                                  String remoteAddr) {
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            int comma = forwardedFor.indexOf(',');
            return (comma > 0 ? forwardedFor.substring(0, comma) : forwardedFor)
                    .trim();
        }
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return remoteAddr == null ? "" : remoteAddr;
    }
}
