package com.example.blog_backend.controller;

import com.example.blog_backend.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传接口。
 * 权限：AuthInterceptor 中 /file/** 已配置为仅管理员可用。
 * 文件落盘到 blog.upload-dir（默认 ./uploads），通过 /uploads/** 静态访问。
 */
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileController {

    /** 允许的图片扩展名白名单（不含 svg：svg 可携带脚本，有 XSS 风险） */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${blog.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return Result.error("只支持上传图片文件");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (extension == null) {
            return Result.error("只支持 jpg / jpeg / png / gif / webp 格式");
        }

        // 目录按月份分组，文件名用 UUID，杜绝路径穿越和重名覆盖
        String month = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));
        String filename = UUID.randomUUID().toString().replace("-", "")
                + "." + extension;

        try {
            Path targetDir = Paths.get(uploadDir).toAbsolutePath()
                    .normalize().resolve(month);
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(filename));
        } catch (IOException e) {
            return Result.error("图片保存失败：" + e.getMessage());
        }

        // 返回相对路径，前端渲染时统一拼接后端地址，方便未来更换域名
        return Result.success("/uploads/" + month + "/" + filename);
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return null;
        }
        String ext = originalFilename
                .substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);
        if (!ext.matches("[a-z0-9]+") ||
                !ALLOWED_EXTENSIONS.contains(ext)) {
            return null;
        }
        return ext;
    }
}
