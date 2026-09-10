
package com.example.blog_backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private IArticleService articleService;

    // 查询全部
    @GetMapping("/selectAll")
    public Result selectAll() {
        if (AuthContext.isManager()) {
            return Result.success(articleService.selectAll());
        }
        return Result.success(articleService.selectPublishedAll());
    }

    // 根据 id 查询文章详情
    @GetMapping("/detail")
    public Result detail(@RequestParam Integer id) {
        Article article = articleService.selectById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        if ("draft".equals(article.getStatus()) &&
                !AuthContext.isManager()) {
            return Result.forbidden();
        }
        // 访客阅读已发布文章时阅读量 +1；管理员预览不计数
        if ("published".equals(article.getStatus()) &&
                !AuthContext.isManager()) {
            articleService.increaseViewCount(id);
            int current = article.getViewCount() == null
                    ? 0 : article.getViewCount();
            article.setViewCount(current + 1);
        }
        return Result.success(article);
    }

    // 查询上一篇 / 下一篇（仅限已发布文章）
    @GetMapping("/neighbors")
    public Result neighbors(@RequestParam Integer id) {
        return Result.success(articleService.selectNeighbors(id));
    }

    // 模糊搜索
    @GetMapping("/selectSearch")
    public Result selectSearch(@RequestParam String articleTitle) {
        if (!AuthContext.isManager()) {
            return Result.success(articleService.selectPublishedSearch(articleTitle));
        }
        return
                Result.success(articleService.selectSearch(articleTitle));
    }

    // 归档：已发布文章按年份分组
    @GetMapping("/archive")
    public Result archive() {
        return Result.success(articleService.selectArchive());
    }

    // 站点统计：已发布文章数 + 总阅读量（写作年数由前端按建站年份计算）
    @GetMapping("/stats")
    public Result stats() {
        return Result.success(articleService.selectStats());
    }

    // 站内搜索：关键词匹配标题/摘要/正文；游客只搜已发布，管理员可搜全部（含草稿）
    @GetMapping("/search")
    public Result search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            // 空关键词不返回全站文章，避免误用成列表接口
            return Result.success(new Page<>(pageNum, pageSize));
        }
        return Result.success(articleService.selectSearchPage(pageNum,
                pageSize, kw, !AuthContext.isManager()));
    }

    // 分页查询（articleTitle / status / categoryId / tagId 均可选；status 仅对管理员生效）
    @GetMapping("/selectPage")
    public Result selectByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String articleTitle,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer tagId) {
        if (!AuthContext.isManager()) {
            return Result.success(articleService.selectPublishedPage(pageNum,
                    pageSize, articleTitle, categoryId, tagId));
        }
        return Result.success(articleService.selectPage(pageNum,
                pageSize, articleTitle, status, categoryId, tagId));
    }

    // 新增 / 编辑（id为null则新增，有id则更新）；返回带 id 的文章对象
    @PostMapping
    public Result insert(@RequestBody Article article) {
        if (article.getId() == null) {
            articleService.insert(article);
        } else {
            articleService.update(article);
        }
        return Result.success(article);
    }

    // 删除
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        articleService.delete(id);
        return Result.success();
    }
}
