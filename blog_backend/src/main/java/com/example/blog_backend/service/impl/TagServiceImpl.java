package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog_backend.dto.TagWithCount;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.entity.ArticleTag;
import com.example.blog_backend.entity.Tag;
import com.example.blog_backend.mapper.ArticleMapper;
import com.example.blog_backend.mapper.ArticleTagMapper;
import com.example.blog_backend.mapper.TagMapper;
import com.example.blog_backend.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements ITagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<TagWithCount> selectAllWithCount() {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName));
        List<TagWithCount> result = new ArrayList<>();
        for (Tag tag : tags) {
            result.add(new TagWithCount(tag.getId(), tag.getName(),
                    countPublishedArticles(tag.getId())));
        }
        return result;
    }

    private long countPublishedArticles(Integer tagId) {
        List<ArticleTag> rows = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getTagId, tagId));
        if (rows.isEmpty()) {
            return 0L;
        }
        List<Integer> articleIds = rows.stream()
                .map(ArticleTag::getArticleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return 0L;
        }
        Long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .in(Article::getId, articleIds)
                .eq(Article::getStatus, "published"));
        return count == null ? 0L : count;
    }

    @Override
    public List<Integer> selectTagIdsByArticle(Integer articleId) {
        if (articleId == null) {
            return new ArrayList<>();
        }
        return articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, articleId))
                .stream()
                .map(ArticleTag::getTagId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> selectTagNamesByArticle(Integer articleId) {
        List<Integer> ids = selectTagIdsByArticle(articleId);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return tagMapper.selectBatchIds(ids).stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<String>> selectNamesByArticleIds(
            List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ArticleTag> rows = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .in(ArticleTag::getArticleId, articleIds));
        if (rows.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> tagIds = rows.stream()
                .map(ArticleTag::getTagId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> nameById = tagIds.isEmpty()
                ? Collections.emptyMap()
                : tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));
        Map<Integer, List<String>> result = new LinkedHashMap<>();
        for (ArticleTag row : rows) {
            String name = nameById.get(row.getTagId());
            if (name == null) {
                continue;
            }
            result.computeIfAbsent(row.getArticleId(),
                    k -> new ArrayList<>()).add(name);
        }
        return result;
    }

    @Override
    public void saveArticleTags(Integer articleId, List<Integer> tagIds) {
        if (articleId == null) {
            return;
        }
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Integer> distinct = tagIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        for (Integer tagId : distinct) {
            articleTagMapper.insert(new ArticleTag(articleId, tagId));
        }
    }

    @Override
    public Tag save(Tag tag) {
        if (tag == null || tag.getName() == null
                || tag.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("标签名不能为空");
        }
        String name = tag.getName().trim();
        if (name.length() > 50) {
            throw new IllegalArgumentException("标签名最长 50 字");
        }
        LambdaQueryWrapper<Tag> sameName = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name);
        Tag existing = tagMapper.selectOne(sameName);
        if (existing != null && (tag.getId() == null
                || !existing.getId().equals(tag.getId()))) {
            throw new IllegalArgumentException("标签已存在");
        }

        tag.setName(name);
        if (tag.getId() == null) {
            tag.setCreateTime(LocalDateTime.now());
            tagMapper.insert(tag);
        } else {
            Tag old = tagMapper.selectById(tag.getId());
            if (old == null) {
                throw new IllegalArgumentException("标签不存在");
            }
            tag.setCreateTime(old.getCreateTime());
            tagMapper.updateById(tag);
        }
        return tag;
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            return;
        }
        tagMapper.deleteById(id);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getTagId, id));
    }
}
