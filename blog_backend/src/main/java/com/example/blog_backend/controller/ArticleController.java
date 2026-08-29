
package com.example.blog_backend.controller;

import com.example.blog_backend.common.AuthContext;
import com.example.blog_backend.common.Result;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = "*")
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

    // 分页查询
    @GetMapping("/selectPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String articleTitle) {
        if (!AuthContext.isManager()) {
            return Result.success(articleService.selectPublishedPage(pageNum,
                    pageSize, articleTitle));
        }
        return Result.success(articleService.selectPage(pageNum,
                pageSize, articleTitle));
    }

    // 新增 / 编辑（id为null则新增，有id则更新）
    @PostMapping
    public Result insert(@RequestBody Article article) {
        if (article.getId() == null) {
            articleService.insert(article);
        } else {
            articleService.update(article);
        }
        return Result.success();
    }

    // 删除
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        articleService.delete(id);
        return Result.success();
    }
}
