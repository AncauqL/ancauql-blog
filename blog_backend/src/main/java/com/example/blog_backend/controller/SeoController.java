package com.example.blog_backend.controller;

import com.example.blog_backend.dto.TagWithCount;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.service.IArticleService;
import com.example.blog_backend.service.ITagService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SEO 相关：sitemap.xml 与 robots.txt。
 * 两者都是公开 GET，不属于 AuthInterceptor 的保护前缀，无需额外放行配置。
 * 站点基址复用 blog.site-url（与 RSS 同一份配置）。
 */
@RestController
public class SeoController {

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private IArticleService articleService;

    @Autowired
    private ITagService tagService;

    @Value("${blog.site-url:http://localhost:8081}")
    private String siteUrl;

    /** 站点地图：首页 / 归档 / 关于我 / 每篇已发布文章 / 有文章的标签页 */
    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public void sitemap(HttpServletResponse response) throws IOException {
        String base = trimTrailingSlash(siteUrl);
        List<Article> articles = articleService.selectPublishedBriefs();
        List<TagWithCount> tags = tagService.selectAllWithCount();

        StringBuilder xml = new StringBuilder(8192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 首页的 lastmod 取最近一次文章更新时间（没有就用今天）
        LocalDate homeLastmod = LocalDate.now();
        for (Article article : articles) {
            LocalDateTime time = lastModified(article);
            if (time != null && time.toLocalDate().isAfter(homeLastmod)) {
                homeLastmod = time.toLocalDate();
            }
        }
        appendUrl(xml, base + "/", homeLastmod, "daily", "1.0");
        appendUrl(xml, base + "/archive", homeLastmod, "weekly", "0.6");
        appendUrl(xml, base + "/aboutme", null, "monthly", "0.5");

        for (Article article : articles) {
            LocalDateTime time = lastModified(article);
            appendUrl(xml, base + "/post/" + article.getId(),
                    time == null ? null : time.toLocalDate(),
                    "monthly", "0.8");
        }

        for (TagWithCount tag : tags) {
            if (tag.getCount() <= 0) {
                continue;
            }
            appendUrl(xml, base + "/?tag=" + tag.getId(), null, "weekly", "0.4");
        }

        xml.append("</urlset>");
        // 直接写 response，必须自己声明 content-type（produces 只管返回值走转换器的场景）
        response.setContentType("application/xml;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(xml.toString());
    }

    /** robots.txt：放行前台，屏蔽后台与搜索页，并声明 sitemap 地址 */
    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    public void robots(HttpServletResponse response) throws IOException {
        String base = trimTrailingSlash(siteUrl);
        StringBuilder txt = new StringBuilder(512);
        txt.append("User-agent: *\n");
        txt.append("Allow: /\n");
        // 后台页面（都要登录，没必要被抓）与站内搜索结果页
        txt.append("Disallow: /login\n");
        txt.append("Disallow: /register\n");
        txt.append("Disallow: /search\n");
        txt.append("Disallow: /dashboard\n");
        txt.append("Disallow: /settings\n");
        txt.append("Disallow: /site\n");
        txt.append("Disallow: /article/edit\n");
        txt.append("Disallow: /comment\n");
        txt.append("\n");
        txt.append("Sitemap: ").append(base).append("/sitemap.xml\n");
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(txt.toString());
    }

    private void appendUrl(StringBuilder xml, String loc, LocalDate lastmod,
                           String changefreq, String priority) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(esc(loc)).append("</loc>\n");
        if (lastmod != null) {
            xml.append("    <lastmod>").append(ISO_DATE.format(lastmod))
               .append("</lastmod>\n");
        }
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }

    /** 有更新时间用更新时间，否则回退创建时间 */
    private LocalDateTime lastModified(Article article) {
        return article.getUpdateTime() != null
                ? article.getUpdateTime() : article.getCreateTime();
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

    /** 极简 XML 转义（loc 里可能出现 & 等字符） */
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
