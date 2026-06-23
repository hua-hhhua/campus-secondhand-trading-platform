package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer userId;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 定时发布相关字段 ==========
    private LocalDateTime publishedAt;  // 发布时间（用于定时发布）
    private Integer isTop;               // 是否置顶 0-否 1-是

    // ========== 允许评论字段 ==========
    private Integer allowComment;        // 是否允许评论 0-不允许 1-允许

    // ========== 是否发送邮件通知 ==========
    private Integer sendEmail;           // 是否发送邮件通知 0-不发送 1-发送

    // ========== 分类ID ==========
    private Integer categoryId;          // 分类ID

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
    public void setIsTop(Integer isTop) {
        this.isTop = isTop != null ? isTop : 0;
    }

    public Integer getAllowComment() { return allowComment; }
    public void setAllowComment(Integer allowComment) {
        this.allowComment = allowComment != null ? allowComment : 0;
    }

    public Integer getSendEmail() { return sendEmail; }
    public void setSendEmail(Integer sendEmail) {
        this.sendEmail = sendEmail != null ? sendEmail : 0;
    }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}