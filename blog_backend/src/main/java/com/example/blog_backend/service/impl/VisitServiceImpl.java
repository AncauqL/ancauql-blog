package com.example.blog_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog_backend.dto.DailyVisit;
import com.example.blog_backend.dto.TopArticle;
import com.example.blog_backend.dto.VisitDashboard;
import com.example.blog_backend.dto.VisitOverview;
import com.example.blog_backend.entity.Article;
import com.example.blog_backend.entity.VisitLog;
import com.example.blog_backend.mapper.ArticleMapper;
import com.example.blog_backend.mapper.VisitLogMapper;
import com.example.blog_backend.service.IVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class VisitServiceImpl implements IVisitService {

    /** 生成访客标识用的盐（只是让哈希不可逆，不是密钥） */
    private static final String SALT = "ancauql-blog-visit";

    /** 明显的爬虫 / 脚本 UA，不计入统计 */
    private static final Pattern BOT_UA = Pattern.compile(
            "(?i).*(bot|spider|crawl|slurp|bingpreview|curl|wget|"
                    + "python-requests|okhttp|httpclient|headless|monitor|"
                    + "uptime|pingdom).*");

    /** 同一访客对同一路径的重复上报窗口（毫秒），防抖 + 防刷 */
    private static final long DEDUPE_WINDOW = 3000L;

    private static final int MAX_PATH = 200;
    private static final int MAX_DEDUPE_ENTRIES = 20000;
    private static final int TOP_LIMIT = 10;

    private final Map<String, Long> recent = new ConcurrentHashMap<>();

    @Autowired
    private VisitLogMapper visitLogMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void record(String path, Integer articleId, String ip,
                       String userAgent) {
        String ua = userAgent == null ? "" : userAgent;
        if (ua.isEmpty() || BOT_UA.matcher(ua).matches()) {
            return;
        }

        String cleanPath = (path == null || path.trim().isEmpty())
                ? "/" : path.trim();
        if (cleanPath.length() > MAX_PATH) {
            cleanPath = cleanPath.substring(0, MAX_PATH);
        }

        String visitorKey = visitorKey(ip, ua);
        if (isDuplicate(visitorKey, cleanPath)) {
            return;
        }

        VisitLog log = new VisitLog();
        log.setStatDate(LocalDate.now());
        log.setPath(cleanPath);
        log.setArticleId(articleId);
        log.setVisitorKey(visitorKey);
        log.setCreateTime(LocalDateTime.now());
        visitLogMapper.insert(log);
    }

    @Override
    public VisitDashboard dashboard(Integer days) {
        int range = days == null ? 30 : days;
        if (range < 1) {
            range = 1;
        }
        if (range > 365) {
            range = 365;
        }

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(range - 1L);
        List<DailyVisit> rows = visitLogMapper.selectDailyBetween(from, today);

        VisitDashboard dashboard = new VisitDashboard();
        dashboard.setDays(range);

        // 缺的日期补 0，趋势图才有连续的横轴
        Map<LocalDate, DailyVisit> byDate = new HashMap<>();
        for (DailyVisit row : rows) {
            byDate.put(row.getStatDate(), row);
        }
        List<DailyVisit> daily = new ArrayList<>();
        long rangePv = 0L;
        long rangeUv = 0L;
        for (int i = 0; i < range; i++) {
            LocalDate date = from.plusDays(i);
            DailyVisit row = byDate.get(date);
            long pv = row == null || row.getPv() == null ? 0L : row.getPv();
            long uv = row == null || row.getUv() == null ? 0L : row.getUv();
            daily.add(new DailyVisit(date, pv, uv));
            rangePv += pv;
            rangeUv += uv;
        }
        dashboard.setDaily(daily);

        VisitOverview overview = new VisitOverview();
        DailyVisit todayRow = byDate.get(today);
        overview.setTodayPv(todayRow == null || todayRow.getPv() == null
                ? 0L : todayRow.getPv());
        overview.setTodayUv(todayRow == null || todayRow.getUv() == null
                ? 0L : todayRow.getUv());
        overview.setRangePv(rangePv);
        overview.setRangeUv(rangeUv);
        Long totalPv = visitLogMapper.selectTotalPv();
        Long totalUv = visitLogMapper.selectTotalUv();
        overview.setTotalPv(totalPv == null ? 0L : totalPv);
        overview.setTotalUv(totalUv == null ? 0L : totalUv);
        dashboard.setOverview(overview);

        dashboard.setTopArticles(attachTitles(
                visitLogMapper.selectTopArticles(from, TOP_LIMIT)));
        return dashboard;
    }

    /** 给热门文章补标题（文章可能已被删除） */
    private List<TopArticle> attachTitles(List<TopArticle> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = new ArrayList<>();
        for (TopArticle row : rows) {
            if (row.getArticleId() != null) {
                ids.add(row.getArticleId());
            }
        }
        Map<Integer, String> titles = new HashMap<>();
        if (!ids.isEmpty()) {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(Article::getId, Article::getTitle);
            wrapper.in(Article::getId, ids);
            for (Article article : articleMapper.selectList(wrapper)) {
                titles.put(article.getId(), article.getTitle());
            }
        }
        for (TopArticle row : rows) {
            String title = titles.get(row.getArticleId());
            row.setTitle(title == null ? "（文章已删除）" : title);
        }
        return rows;
    }

    /** 同一访客 + 同一路径在窗口内只记一次 */
    private boolean isDuplicate(String visitorKey, String path) {
        if (recent.size() > MAX_DEDUPE_ENTRIES) {
            recent.clear();
        }
        String key = visitorKey + '|' + path;
        long now = System.currentTimeMillis();
        Long last = recent.get(key);
        if (last != null && now - last < DEDUPE_WINDOW) {
            return true;
        }
        recent.put(key, now);
        return false;
    }

    /** 访客标识：md5(IP + UA + 盐)，不保存原始 IP */
    private String visitorKey(String ip, String userAgent) {
        String raw = (ip == null ? "" : ip) + '|' + userAgent + '|' + SALT;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(
                    raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            // 理论上不会发生；退化成 UA 的 hashCode，保证不影响访问
            return String.format("%032x", raw.hashCode());
        }
    }
}
