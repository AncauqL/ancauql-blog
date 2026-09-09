package com.example.blog_backend.controller;

import com.example.blog_backend.common.Result;
import com.example.blog_backend.dto.CommentAddRequest;
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
@CrossOrigin(origins = "*")
public class CommentController {

    /** IP -> [窗口起始毫秒, 窗口内已发条数]，简易滑动窗口限流 */
    private static final ConcurrentHashMap<String, long[]> LIMITER =
            new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_PER_WINDOW = 8;

    @Autowired
    private ICommentService commentService;

    // 某篇文章的评论（公开，按时间升序）
    @GetMapping
    public Result list(@RequestParam Integer articleId) {
        return Result.success(commentService.selectByArticle(articleId));
    }

    // 管理端：全部评论（新的在前）
    @GetMapping("/list")
    public Result adminList() {
        List<Comment> all = commentService.selectAll();
        return Result.success(all);
    }

    // 发表评论（公开）
    @PostMapping
    public Result add(HttpServletRequest request,
                      @RequestBody CommentAddRequest req) {
        // 蜜罐命中：静默丢弃，返回成功但不落库（迷惑灌水脚本）
        if (req.getWebsite() != null && !req.getWebsite().trim().isEmpty()) {
            return Result.success();
        }
        if (!allow(request.getRemoteAddr())) {
            return Result.error("评论太频繁，请稍后再试");
        }

        if (req.getArticleId() == null) {
            return Result.error("缺少文章信息");
        }
        String nickname = trim(req.getNickname());
        String content = trim(req.getContent());
        if (nickname.isEmpty() || content.isEmpty()) {
            return Result.error("昵称和内容不能为空");
        }
        if (nickname.length() > 40) {
            return Result.error("昵称最长 40 字");
        }
        if (content.length() > 2000) {
            return Result.error("评论最长 2000 字");
        }

        Comment comment = new Comment();
        comment.setArticleId(req.getArticleId());
        comment.setNickname(nickname);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        commentService.insert(comment);
        return Result.success();
    }

    // 删除评论（管理员）
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        commentService.delete(id);
        return Result.success();
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
