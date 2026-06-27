package com.campus.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章扩展实体类 - 用于MyBatis ResultMap关联查询
 * 包含作者对象和分类对象（支持懒加载）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResultMapVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String title;

    private String slug;

    private String content;

    private String summary;

    private String coverImage;

    private Integer status;  // 0:草稿 1:已发布 2:定时发布

    private Integer viewCount;

    private Integer isTop;  // 0:不置顶 1:置顶

    private Integer allowComment;

    private LocalDateTime publishedAt;

    private Integer userId;  // 作者ID，外键

    private Integer categoryId;  // 分类ID，外键

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer sendEmail;

    // ========== 扩展属性：关联对象（支持懒加载） ==========

    /**
     * 作者信息（一对一关联）
     * 使用懒加载，只有在访问时才会查询数据库
     */
    private User author;

    /**
     * 分类信息（一对一关联）
     * 使用懒加载，只有在访问时才会查询数据库
     */
    private Category category;
}