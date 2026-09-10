package com.example.blog_backend.service.impl;

import com.example.blog_backend.entity.SiteConfig;
import com.example.blog_backend.mapper.SiteConfigMapper;
import com.example.blog_backend.service.ISiteConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SiteConfigServiceImpl implements ISiteConfigService {

    private static final Logger log =
            LoggerFactory.getLogger(SiteConfigServiceImpl.class);

    /** 固定单行主键 */
    private static final int ROW_ID = 1;
    /** 整段 JSON 上限（防止把配置当文件柜用） */
    private static final int MAX_BYTES = 64 * 1024;
    /** 单条文本上限 */
    private static final int MAX_TEXT = 500;
    /** 普通列表条数上限 */
    private static final int MAX_LIST = 200;

    private static final Set<String> TOP_KEYS = new HashSet<>(Arrays.asList(
            "name", "author", "slogan", "heroTitleLine1", "heroTitleLine2",
            "heroText", "identity", "portrait", "icp", "startYear",
            "aboutLines", "socials", "profile"));

    private static final Set<String> SOCIAL_KEYS = new HashSet<>(Arrays.asList(
            "github", "bilibili", "email", "qq"));

    private static final Set<String> PROFILE_TEXT_KEYS = new HashSet<>(Arrays.asList(
            "name", "identity", "motto"));

    private static final Set<String> PROFILE_LIST_KEYS = new HashSet<>(Arrays.asList(
            "bio", "interests", "favorites"));

    private static final Set<String> PROFILE_SKILL_KEYS = new HashSet<>(Arrays.asList(
            "label", "desc"));

    private static final Set<String> PROFILE_JOURNEY_KEYS = new HashSet<>(Arrays.asList(
            "period", "text"));

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SiteConfigMapper siteConfigMapper;

    @Override
    public Map<String, Object> getConfig() {
        SiteConfig row = siteConfigMapper.selectById(ROW_ID);
        if (row == null || row.getContent() == null
                || row.getContent().trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    row.getContent(),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return parsed == null ? new LinkedHashMap<>() : parsed;
        } catch (Exception e) {
            // 数据坏了不能让前台白屏：退回代码默认值，并留下线索
            log.warn("站点配置 JSON 解析失败，已回退代码默认值: {}",
                    e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Object> saveConfig(Map<String, Object> config) {
        Map<String, Object> normalized = validate(config);

        String json;
        try {
            json = objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("站点配置无法保存");
        }
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new IllegalArgumentException("站点配置过大（上限 64KB）");
        }

        SiteConfig row = siteConfigMapper.selectById(ROW_ID);
        SiteConfig entity = new SiteConfig();
        entity.setId(ROW_ID);
        entity.setContent(json);
        entity.setUpdateTime(LocalDateTime.now());
        if (row == null) {
            siteConfigMapper.insert(entity);
        } else {
            siteConfigMapper.updateById(entity);
        }
        return normalized;
    }

    /* ---------- 校验 ---------- */

    private Map<String, Object> validate(Map<String, Object> config) {
        Map<String, Object> source = config == null
                ? new LinkedHashMap<>() : config;
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!TOP_KEYS.contains(key)) {
                throw new IllegalArgumentException("未知的配置项：" + key);
            }
            // 前端清空某项时会传空字符串，这里保留（空字符串 = 该项不显示）
            switch (key) {
                case "startYear":
                    result.put(key, toYear(value));
                    break;
                case "aboutLines":
                    result.put(key, toTextList(key, value, MAX_LIST));
                    break;
                case "socials":
                    result.put(key, toSocials(value));
                    break;
                case "profile":
                    result.put(key, toProfile(value));
                    break;
                default:
                    result.put(key, toText(key, value, MAX_TEXT));
                    break;
            }
        }
        return result;
    }

    private String toText(String key, Object value, int max) {
        if (value == null) {
            return "";
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(label(key) + "必须是文本");
        }
        String text = ((String) value).trim();
        if (text.length() > max) {
            throw new IllegalArgumentException(
                    label(key) + "最长 " + max + " 字");
        }
        return text;
    }

    private Integer toYear(Object value) {
        if (value == null || "".equals(value)) {
            return null;
        }
        if (value instanceof Number) {
            int year = ((Number) value).intValue();
            if (year < 1970 || year > 2100) {
                throw new IllegalArgumentException("建站年份需在 1970-2100 之间");
            }
            return year;
        }
        if (value instanceof String) {
            String text = ((String) value).trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                int year = Integer.parseInt(text);
                if (year < 1970 || year > 2100) {
                    throw new IllegalArgumentException(
                            "建站年份需在 1970-2100 之间");
                }
                return year;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("建站年份必须是数字");
            }
        }
        throw new IllegalArgumentException("建站年份必须是数字");
    }

    private List<String> toTextList(String key, Object value, int maxItems) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        if (!(value instanceof List)) {
            throw new IllegalArgumentException(label(key) + "必须是文本列表");
        }
        List<?> list = (List<?>) value;
        if (list.size() > maxItems) {
            throw new IllegalArgumentException(
                    label(key) + "最多 " + maxItems + " 条");
        }
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            if (!(item instanceof String)) {
                throw new IllegalArgumentException(
                        label(key) + "里的每一项都必须是文本");
            }
            String text = ((String) item).trim();
            if (text.isEmpty()) {
                continue;
            }
            if (text.length() > MAX_TEXT) {
                throw new IllegalArgumentException(
                        label(key) + "里的单条最长 " + MAX_TEXT + " 字");
            }
            result.add(text);
        }
        return result;
    }

    private Map<String, Object> toSocials(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value == null) {
            return result;
        }
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("社交信息必须是对象");
        }
        Map<?, ?> map = (Map<?, ?>) value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (!SOCIAL_KEYS.contains(key)) {
                throw new IllegalArgumentException("未知的社交项：" + key);
            }
            result.put(key, toText("socials." + key, entry.getValue(), MAX_TEXT));
        }
        return result;
    }

    private Map<String, Object> toProfile(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value == null) {
            return result;
        }
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("个人资料必须是对象");
        }
        Map<?, ?> map = (Map<?, ?>) value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object item = entry.getValue();
            if (PROFILE_TEXT_KEYS.contains(key)) {
                result.put(key, toText("profile." + key, item, MAX_TEXT));
            } else if (PROFILE_LIST_KEYS.contains(key)) {
                result.put(key, toTextList("profile." + key, item, MAX_LIST));
            } else if ("skills".equals(key)) {
                result.put(key, toPairList(key, item,
                        PROFILE_SKILL_KEYS, "label", "desc"));
            } else if ("journey".equals(key)) {
                result.put(key, toPairList(key, item,
                        PROFILE_JOURNEY_KEYS, "period", "text"));
            } else {
                throw new IllegalArgumentException("未知的个人资料项：" + key);
            }
        }
        return result;
    }

    /** skills / journey 这类 [{a,b}] 结构 */
    private List<Map<String, String>> toPairList(String key, Object value,
                                                 Set<String> allowed,
                                                 String first,
                                                 String second) {
        List<Map<String, String>> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        if (!(value instanceof List)) {
            throw new IllegalArgumentException(label(key) + "必须是列表");
        }
        List<?> list = (List<?>) value;
        if (list.size() > MAX_LIST) {
            throw new IllegalArgumentException(
                    label(key) + "最多 " + MAX_LIST + " 条");
        }
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            if (!(item instanceof Map)) {
                throw new IllegalArgumentException(
                        label(key) + "里的每一项都必须是对象");
            }
            Map<?, ?> map = (Map<?, ?>) item;
            Map<String, String> row = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String field = String.valueOf(entry.getKey());
                if (!allowed.contains(field)) {
                    throw new IllegalArgumentException(
                            label(key) + " 不支持字段：" + field);
                }
                row.put(field, toText(key + "." + field,
                        entry.getValue(), MAX_TEXT));
            }
            String firstValue = row.get(first);
            String secondValue = row.get(second);
            boolean blank = (firstValue == null || firstValue.isEmpty())
                    && (secondValue == null || secondValue.isEmpty());
            if (blank) {
                continue;
            }
            result.add(row);
        }
        return result;
    }

    private String label(String key) {
        switch (key) {
            case "name":
                return "站点名称";
            case "author":
                return "作者署名";
            case "slogan":
                return "slogan";
            case "heroTitleLine1":
                return "首页大标题第一行";
            case "heroTitleLine2":
                return "首页大标题第二行";
            case "heroText":
                return "首页描述";
            case "identity":
                return "身份一行";
            case "portrait":
                return "肖像图地址";
            case "icp":
                return "备案号";
            case "aboutLines":
                return "首页简介";
            case "bio":
                return "自我介绍";
            case "interests":
                return "兴趣爱好";
            case "favorites":
                return "喜欢的作品";
            case "skills":
                return "技术栈";
            case "journey":
                return "经历";
            default:
                return key;
        }
    }
}
