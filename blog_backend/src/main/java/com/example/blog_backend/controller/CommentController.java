package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.common.VisitorKeyUtil;
import com.example.blog_backend.dto.CommentAddRequest;
import com.example.blog_backend.dto.CommentLikeRequest;
import com.example.blog_backend.dto.UserProfile;
import com.example.blog_backend.entity.Comment;
import com.example.blog_backend.service.ICommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 评论接口。
 * - 读取某篇文章评论 / 发表评论：公开（无需登录，即发即显）。
 * - 管理端列表与删除：仅管理员（见 AuthInterceptor）。
 * 反垃圾：蜜罐字段 + 按 IP 的简易内存限流。
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    /** IP -> [窗口起始毫秒, 窗口内已发条数]，简易滑动窗口限流 */
    private static final ConcurrentHashMap<String, long[]> LIMITER =
            new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_PER_WINDOW = 8;

    @Autowired
    private ICommentService commentService;

    // 某篇文章的评论（公开，顶层按时间升序、回复紧跟其顶层）
    @GetMapping
    public Result list(@RequestParam Integer articleId,
                       HttpServletRequest request) {
        return Result.success(commentService.selectByArticle(articleId,
                visitorKey(request)));
    }

    // 管理端：全部评论（新的在前）
    @GetMapping("/list")
    public Result adminList() {
        List<Comment> all = commentService.selectAll();
        return Result.success(all);
    }

    // 点赞 / 取消点赞（公开，按访客标识去重，同一人只能点一次）
    @PostMapping("/like")
    public Result like(HttpServletRequest request,
                       @RequestBody CommentLikeRequest req) {
        if (req == null || req.getCommentId() == null) {
            return Result.error("缺少评论信息");
        }
        try {
            return Result.success(commentService.toggleLike(
                    req.getCommentId(), visitorKey(request)));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    // 发表评论（公开）
    @PostMapping
    public Result add(HttpServletRequest request,
                      @RequestBody CommentAddRequest req) {
        // 蜜罐命中：静默丢弃，返回成功但不落库（迷惑灌水脚本）
        if (req.getWebsite() != null && !req.getWebsite().trim().isEmpty()) {
            return Result.success();
        }
        if (!allow(clientIp(request))) {
            return Result.error("评论太频繁，请稍后再试");
        }

        if (req.getArticleId() == null) {
            return Result.error("缺少文章信息");
        }
        String content = trim(req.getContent());
        if (content.isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        if (content.length() > 2000) {
            return Result.error("评论最长 2000 字");
        }

        // 回复：校验上级评论存在且属于同一篇文章，并统一挂到顶层评论下（只做两级）
        Integer parentId = null;
        String replyToNickname = null;
        if (req.getParentId() != null) {
            Comment parent = commentService.selectById(req.getParentId());
            if (parent == null) {
                return Result.error("要回复的评论不存在");
            }
            if (!parent.getArticleId().equals(req.getArticleId())) {
                return Result.error("要回复的评论不属于这篇文章");
            }
            parentId = parent.getParentId() == null
                    ? parent.getId() : parent.getParentId();
            replyToNickname = parent.getNickname();
        }

        // 发表评论需登录（拦截器已保证）；昵称取账号昵称，服务端定名，防止伪造
        UserProfile user = AuthContext.getUser();
        String nickname = pickNickname(user);

        Comment comment = new Comment();
        comment.setArticleId(req.getArticleId());
        comment.setUserId(user.getId());
        comment.setNickname(nickname);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setReplyToNickname(replyToNickname);
        comment.setCreateTime(LocalDateTime.now());
        commentService.insert(comment);
        return Result.success();
    }

    // 删除评论：管理员可删任意；普通用户只能删自己发的
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        Comment existing = commentService.selectById(id);
        if (existing == null) {
            return Result.error("评论不存在");
        }
        UserProfile user = AuthContext.getUser();
        boolean manager = AuthContext.isManager();
        boolean owner = existing.getUserId() != null
                && existing.getUserId().equals(user.getId());
        if (!manager && !owner) {
            return Result.forbidden();
        }
        commentService.delete(id);
        return Result.success();
    }

    /** 当前访客标识：登录用 u{id}，未登录用 md5(IP+UA+盐) */
    private static String visitorKey(HttpServletRequest request) {
        UserProfile user = AuthContext.getUser();
        Integer userId = user == null ? null : user.getId();
        return VisitorKeyUtil.of(userId, clientIp(request),
                request.getHeader("User-Agent"));
    }

    /** nginx 反代时真实 IP 在 X-Forwarded-For 的第一段 */
    private static String clientIp(HttpServletRequest request) {
        return VisitorKeyUtil.clientIp(
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr());
    }

    private static String pickNickname(UserProfile user) {        if (user == null) {
            return "匿名";
        }
        String nick = user.getNickname() == null ? "" : user.getNickname().trim();
        if (!nick.isEmpty()) {
            return nick.length() > 40 ? nick.substring(0, 40) : nick;
        }
        String fallback = user.getUsername() == null ? "" : user.getUsername().trim();
        return fallback.isEmpty() ? "匿名" : fallback;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean allow(String ip) {
        String key = ip == null || ip.isEmpty() ? "unknown" : ip;
        long now = System.currentTimeMillis();
        synchronized (LIMITER) {
            long[] bucket = LIMITER.get(key);
            if (bucket == null || now - bucket[0] >= WINDOW_MS) {
                LIMITER.put(key, new long[]{now, 1});
                return true;
            }
            if (bucket[1] >= MAX_PER_WINDOW) {
                return false;
            }
            bucket[1]++;
            return true;
        }
    }
}
