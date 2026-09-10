package com.example.blog_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    @Value("${blog.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 允许跨域的前端来源（逗号分隔）。
     * 本地开发默认 8080/8081；生产建议同域反代（此时可留空）。
     */
    @Value("${blog.cors-allowed-origins:http://localhost:8080,http://localhost:8081}")
    private String corsAllowedOrigins;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String value = corsAllowedOrigins == null ? "" : corsAllowedOrigins.trim();
        String[] origins = value.isEmpty()
                ? new String[0]
                : value.split("\\s*,\\s*");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/hello");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传图片的静态访问：/uploads/** -> 本地 blog.upload-dir 目录
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = root.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
