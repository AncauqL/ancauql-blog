package com.example.blog_backend.dto;

/**
 * 文章详情页“上一篇 / 下一篇”导航数据。
 * 只携带跳转所需的最小字段，避免把正文一并返回。
 */
public class ArticleNeighbors {

    /** 上一篇：发布时间更早的一篇，没有则为 null */
    private ArticleBrief prev;

    /** 下一篇：发布时间更晚的一篇，没有则为 null */
    private ArticleBrief next;

    public ArticleNeighbors() {
    }

    public ArticleNeighbors(ArticleBrief prev, ArticleBrief next) {
        this.prev = prev;
        this.next = next;
    }

    public ArticleBrief getPrev() {
        return prev;
    }

    public void setPrev(ArticleBrief prev) {
        this.prev = prev;
    }

    public ArticleBrief getNext() {
        return next;
    }

    public void setNext(ArticleBrief next) {
        this.next = next;
    }

    public static class ArticleBrief {
        private Integer id;
        private String title;

        public ArticleBrief() {
        }

        public ArticleBrief(Integer id, String title) {
            this.id = id;
            this.title = title;
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
    }
}
