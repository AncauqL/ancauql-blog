package com.example.blog_backend.service;

import com.example.blog_backend.dto.UserProfile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private static final int EXPIRE_HOURS = 8;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String createToken(UserProfile user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(user,
                LocalDateTime.now().plusHours(EXPIRE_HOURS)));
        return token;
    }

    public UserProfile resolve(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }

        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            return null;
        }

        return session.user;
    }

    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private static class Session {
        private final UserProfile user;
        private final LocalDateTime expiresAt;

        private Session(UserProfile user, LocalDateTime expiresAt) {
            this.user = user;
            this.expiresAt = expiresAt;
        }
    }
}
