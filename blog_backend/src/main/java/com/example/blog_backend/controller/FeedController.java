package com.example.blog_backend.controller;

import com.example.blog_backend.entity.Article;
import com.example.blog_backend.service.IArticleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 公开的 RSS 订阅源：GET /feed.xml，只收录已发布文章。
 * AuthInterceptor 对 /feed 的 GET 天然放行（不属于 /user、/file、
 * /article 写操作等受保护前缀），无需额外配置。
 */
@RestController
public class FeedController {

    private static final DateTimeFormatter RFC1123 =
            DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US);
    private static final int MAX_ITEMS = 50;

    @Autowired
    private IArticleService articleService;

    /** RSS 频道标题（可在 application.yml 的 blog.site-title 覆盖） */
    @Value("${blog.site-title:AncauqL 的个人博客}")
    private String siteTitle;

    /**
     * 前台站点基址，用于拼每篇文章的链接。
     * 默认 http://localhost:8081（本机前端实际端口）；换域名时用
     * BLOG_SITE_URL 或 application.yml 的 blog.site-url 配置。
     */
    @Value("${blog.site-url:http://localhost:8081}")
    private String siteUrl;

    @GetMapping(value = "/feed.xml")
    public void feed(HttpServletResponse response) throws IOException {
        // 显式声明 RSS 内容类型，浏览器/阅读器才能正确识别为 feed
        response.setContentType("application/rss+xml;charset=UTF-8");

        List<Article> articles = articleService.selectPublishedAll();
        // 最新的在前；createTime 为 null 兜底放最后
        articles.sort(Comparator.comparing(Article::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        String base = trimTrailingSlash(siteUrl);
        String title = esc(siteTitle == null ? "Blog" : siteTitle);

        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("  <channel>\n");
        xml.append("    <title>").append(title).append("</title>\n");
        xml.append("    <link>").append(esc(base)).append("</link>\n");
        xml.append("    <description>").append(title).append("</description>\n");
        xml.append("    <language>zh-cn</language>\n");
        xml.append("    <lastBuildDate>")
           .append(rfc1123(ZonedDateTime.now(ZoneId.of("UTC"))))
           .append("</lastBuildDate>\n");
        xml.append("    <atom:link href=\"").append(esc(base))
           .append("/feed.xml\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        int count = 0;
        for (Article article : articles) {
            if (count >= MAX_ITEMS) {
                break;
            }
            String url = base + "/post/" + article.getId();
            xml.append("    <item>\n");
            xml.append("      <title>").append(esc(article.getTitle())).append("</title>\n");
            xml.append("      <link>").append(esc(url)).append("</link>\n");
            xml.append("      <guid isPermaLink=\"true\">").append(esc(url)).append("</guid>\n");
            if (article.getCreateTime() != null) {
                xml.append("      <pubDate>")
                   .append(rfc1123(article.getCreateTime().atZone(ZoneId.of("UTC"))))
                   .append("</pubDate>\n");
            }
            if (article.getSummary() != null && !article.getSummary().isBlank()) {
                xml.append("      <description>")
                   .append(esc(article.getSummary()))
                   .append("</description>\n");
            }
            xml.append("    </item>\n");
            count++;
        }

        xml.append("  </channel>\n");
        xml.append("</rss>");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(xml.toString());
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String s = value.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String rfc1123(ZonedDateTime time) {
        return RFC1123.format(time);
    }

    /** 极简 XML 转义，防止注入。 */
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
