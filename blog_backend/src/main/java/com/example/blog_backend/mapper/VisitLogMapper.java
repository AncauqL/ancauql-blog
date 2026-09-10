package com.example.blog_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog_backend.dto.DailyVisit;
import com.example.blog_backend.dto.TopArticle;
import com.example.blog_backend.entity.VisitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {

    /** 区间内按天汇总（PV / 去重访客数） */
    @Select("SELECT stat_date AS statDate, COUNT(*) AS pv, "
            + "COUNT(DISTINCT visitor_key) AS uv FROM visit_log "
            + "WHERE stat_date BETWEEN #{from} AND #{to} "
            + "GROUP BY stat_date ORDER BY stat_date")
    List<DailyVisit> selectDailyBetween(@Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

    /** 区间内最热文章（只统计文章页记录） */
    @Select("SELECT article_id AS articleId, COUNT(*) AS pv FROM visit_log "
            + "WHERE article_id IS NOT NULL AND stat_date >= #{from} "
            + "GROUP BY article_id ORDER BY pv DESC, article_id DESC "
            + "LIMIT #{limit}")
    List<TopArticle> selectTopArticles(@Param("from") LocalDate from,
                                       @Param("limit") int limit);

    /** 累计浏览量 */
    @Select("SELECT COUNT(*) FROM visit_log")
    Long selectTotalPv();

    /** 累计独立访客（跨天按 visitor_key 去重） */
    @Select("SELECT COUNT(DISTINCT visitor_key) FROM visit_log")
    Long selectTotalUv();
}
