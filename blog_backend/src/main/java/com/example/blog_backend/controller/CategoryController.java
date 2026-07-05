
package com.example.blog_backend.controller;

import com.example.blog_backend.common.Result;
import com.example.blog_backend.entity.Category;
import com.example.blog_backend.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    // 查询全部
    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(categoryService.selectAll());
    }

    // 模糊搜索
    @GetMapping("/selectSearch")
    public Result selectSearch(@RequestParam String categoryName) {
        return
                Result.success(categoryService.selectSearch(categoryName));
    }

    // 分页查询
    @GetMapping("/selectPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String categoryName) {
        return Result.success(categoryService.selectPage(pageNum,
                pageSize, categoryName));
    }

    // 新增 / 编辑（id为null则新增，有id则更新）
    @PostMapping
    public Result insert(@RequestBody Category category) {
        if (category.getId() == null) {
            categoryService.insert(category);
        } else {
            categoryService.update(category);
        }
        return Result.success();
    }

    // 删除
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        categoryService.delete(id);
        return Result.success();
    }
}
