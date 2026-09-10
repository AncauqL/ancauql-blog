package com.example.blog_backend.service;

import java.util.Map;

/**
 * 站点信息配置：公开读、管理员写。
 * 返回/接收的结构与前端 config/site.js 的 SITE 对齐（Map 形式，缺省的 key 走代码默认值）。
 */
public interface ISiteConfigService {

    /** 读取后台配置；未配置或内容损坏时返回空 Map（前端回退代码默认值）。 */
    Map<String, Object> getConfig();

    /** 保存后台配置：先做 key 白名单 + 类型校验，再整段覆盖写入；返回规范化后的配置。 */
    Map<String, Object> saveConfig(Map<String, Object> config);
}
