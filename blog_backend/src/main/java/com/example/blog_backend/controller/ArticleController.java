
package com.example.blog_backend.controller;

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
        return Result.success(articleService.selectAll());
    }

    // 根据 id 查询文章详情
    @GetMapping("/detail")
    public Result detail(@RequestParam Integer id) {
        return Result.success(articleService.selectById(id));
    }

    // 模糊搜索
    @GetMapping("/selectSearch")
    public Result selectSearch(@RequestParam String articleTitle) {
        return
                Result.success(articleService.selectSearch(articleTitle));
    }

    // 分页查询
    @GetMapping("/selectPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String articleTitle) {
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
