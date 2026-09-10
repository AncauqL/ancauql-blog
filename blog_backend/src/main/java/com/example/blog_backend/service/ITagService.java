package com.example.blog_backend.service;

import com.example.blog_backend.dto.TagWithCount;
import com.example.blog_backend.entity.Tag;

import java.util.List;
import java.util.Map;

public interface ITagService {

    /** 全部标签 + 已发布文章数（前台标签云） */
    List<TagWithCount> selectAllWithCount();

    /** 某文章关联的标签 id */
    List<Integer> selectTagIdsByArticle(Integer articleId);

    /** 某文章关联的标签名 */
    List<String> selectTagNamesByArticle(Integer articleId);

    /** 批量取文章标签（articleId -> [标签名]），供列表展示 */
    Map<Integer, List<String>> selectNamesByArticleIds(List<Integer> articleIds);

    /** 保存文章标签（先清后插） */
    void saveArticleTags(Integer articleId, List<Integer> tagIds);

    /** 新增/重命名标签（id 为空则新增；名称重复会抛 IllegalArgumentException） */
    Tag save(Tag tag);

    /** 删除标签（同时清理文章关联） */
    void delete(Integer id);
}
