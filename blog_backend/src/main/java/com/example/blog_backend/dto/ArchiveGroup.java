package com.example.blog_backend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 归档页数据：按年份分组的已发布文章（倒序）。
 */
public class ArchiveGroup {

    private Integer year;

    private List<ArchiveItem> articles = new ArrayList<>();

    public ArchiveGroup() {
    }

    public ArchiveGroup(Integer year) {
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<ArchiveItem> getArticles() {
        return articles;
    }

    public void setArticles(List<ArchiveItem> articles) {
        this.articles = articles;
    }

    public static class ArchiveItem {
        private Integer id;
        private String title;
        private LocalDateTime createTime;

        public ArchiveItem() {
        }

        public ArchiveItem(Integer id, String title,
                           LocalDateTime createTime) {
            this.id = id;
            this.title = title;
            this.createTime = createTime;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}
