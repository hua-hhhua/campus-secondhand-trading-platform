package com.campus.trade.constant;

/**
 * 文章状态常量类
 */
public class ArticleStatus {

    /**
     * 草稿
     */
    public static final Integer DRAFT = 0;

    /**
     * 已发布
     */
    public static final Integer PUBLISHED = 1;

    /**
     * 定时发布
     */
    public static final Integer SCHEDULED = 2;

    /**
     * 已归档
     */
    public static final Integer ARCHIVED = 3;

    /**
     * 根据状态码获取状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) return "未知";

        // 改用 if-else 替代 switch
        if (status.equals(DRAFT)) {
            return "草稿";
        } else if (status.equals(PUBLISHED)) {
            return "已发布";
        } else if (status.equals(SCHEDULED)) {
            return "定时发布";
        } else if (status.equals(ARCHIVED)) {
            return "已归档";
        } else {
            return "未知";
        }
    }
}