package com.example.blog_backend.dto;

/** 标签 + 已发布文章数（前台标签云/筛选用） */
public class TagWithCount {

    private Integer id;
    private String name;
    private Long count;

    public TagWithCount() {
    }

    public TagWithCount(Integer id, String name, Long count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
