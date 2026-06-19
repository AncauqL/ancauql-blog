package com.example.blog_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.blog_backend.mapper")
public class BlogBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogBackendApplication.class,
                args);
    }
}