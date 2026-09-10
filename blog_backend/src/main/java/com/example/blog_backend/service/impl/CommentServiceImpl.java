package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog_backend.entity.Comment;
import com.example.blog_backend.entity.CommentLike;
import com.example.blog_backend.mapper.CommentLikeMapper;
import com.example.blog_backend.mapper.CommentMapper;
import com.example.blog_backend.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommentServiceImpl implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    public List<Comment> selectByArticle(Integer articleId, String visitorKey) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId);
        wrapper.orderByAsc(Comment::getCreateTime);
        wrapper.orderByAsc(Comment::getId);
        List<Comment> rows = commentMapper.selectList(wrapper);

        fillLiked(rows, visitorKey);
        return groupByThread(rows);
    }

    @Override
    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    @Override
    public void insert(Comment comment) {
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0);
        }
        commentMapper.insert(comment);
    }

    @Override
    public List<Comment> selectAll() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        wrapper.orderByDesc(Comment::getId);
        List<Comment> rows = commentMapper.selectList(wrapper);
        fillLiked(rows, null);
        return rows;
    }

    @Override
    public int delete(Integer id) {
        if (id == null) {
            return 0;
        }
        // 顶层评论连同它的回复一起删；删回复本身则只删这一条
        List<Comment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id));
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        for (Comment reply : replies) {
            ids.add(reply.getId());
        }
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
                .in(CommentLike::getCommentId, ids));
        return commentMapper.deleteByIds(ids);    }

    @Override
    public Map<String, Object> toggleLike(Integer commentId, String visitorKey) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        LambdaQueryWrapper<CommentLike> existing =
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getVisitorKey, visitorKey);
        CommentLike liked = commentLikeMapper.selectOne(existing);

        boolean nowLiked;
        if (liked == null) {
            commentLikeMapper.insert(new CommentLike(commentId, visitorKey));
            changeLikeCount(commentId, 1);
            nowLiked = true;
        } else {
            commentLikeMapper.delete(existing);
            changeLikeCount(commentId, -1);
            nowLiked = false;
        }

        Comment latest = commentMapper.selectById(commentId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("likeCount", latest == null || latest.getLikeCount() == null
                ? 0 : latest.getLikeCount());
        result.put("liked", nowLiked);
        return result;
    }

    /**
     * 点赞数在数据库端原子加减（并且不会掉到负数），并发点赞不会丢计数。
     */
    private void changeLikeCount(Integer commentId, int delta) {
        UpdateWrapper<Comment> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", commentId);
        if (delta > 0) {
            wrapper.setSql("like_count = IFNULL(like_count, 0) + 1");
        } else {
            wrapper.setSql("like_count = GREATEST(IFNULL(like_count, 0) - 1, 0)");
        }
        commentMapper.update(null, wrapper);
    }

    /**
     * 顶层评论按时间升序，每条顶层评论后面紧跟它的回复（回复之间也按时间升序）。
     * 前端拿到这个顺序直接渲染即可，不用再自己拼树。
     */
    private List<Comment> groupByThread(List<Comment> rows) {
        List<Comment> roots = new ArrayList<>();
        Map<Integer, List<Comment>> repliesByRoot = new LinkedHashMap<>();
        for (Comment row : rows) {
            if (row.getParentId() == null) {
                roots.add(row);
            } else {
                repliesByRoot.computeIfAbsent(row.getParentId(),
                        k -> new ArrayList<>()).add(row);
            }
        }
        List<Comment> result = new ArrayList<>(rows.size());
        Set<Integer> rootIds = new HashSet<>();
        for (Comment root : roots) {
            rootIds.add(root.getId());
            result.add(root);
            List<Comment> replies = repliesByRoot.get(root.getId());
            if (replies != null) {
                result.addAll(replies);
            }
        }
        // 兜底：父评论已被删但回复还在（历史数据/异常情况），单独附在最后
        for (Map.Entry<Integer, List<Comment>> entry : repliesByRoot.entrySet()) {
            if (!rootIds.contains(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    /** 回填 liked（visitorKey 为空时统一 false） */
    private void fillLiked(List<Comment> rows, String visitorKey) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Integer> likedIds = Collections.emptySet();
        if (visitorKey != null && !visitorKey.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            for (Comment row : rows) {
                ids.add(row.getId());
            }
            likedIds = new HashSet<>();
            List<CommentLike> likes = commentLikeMapper.selectList(
                    new LambdaQueryWrapper<CommentLike>()
                            .eq(CommentLike::getVisitorKey, visitorKey)
                            .in(CommentLike::getCommentId, ids));
            for (CommentLike like : likes) {
                likedIds.add(like.getCommentId());
            }
        }
        for (Comment row : rows) {
            row.setLiked(likedIds.contains(row.getId()));
            if (row.getLikeCount() == null) {
                row.setLikeCount(0);
            }
        }
    }
}
