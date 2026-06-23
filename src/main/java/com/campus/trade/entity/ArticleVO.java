package com.campus.trade.entity;

import java.time.LocalDateTime;

/**
 * 文章扩展实体类（用于多表查询）
 */
public class ArticleVO {

    private Integer id;
    private String title;
    private String content;
    private Integer userId;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishedAt;
    private Integer isTop;
    private Integer allowComment;
    private Integer sendEmail;
    private Integer categoryId;

    // 扩展字段（关联查询）
    private String authorName;      // 作者昵称
    private String authorAvatar;    // 作者头像
    private String categoryName;    // 分类名称
    private String categorySlug;    // 分类别名

    // ========== Getters and Setters ==========

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }

    public Integer getAllowComment() { return allowComment; }
    public void setAllowComment(Integer allowComment) { this.allowComment = allowComment; }

    public Integer getSendEmail() { return sendEmail; }
    public void setSendEmail(Integer sendEmail) { this.sendEmail = sendEmail; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    // ========== 扩展字段的 Getters and Setters ==========

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
}