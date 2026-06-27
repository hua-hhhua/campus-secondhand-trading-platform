package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("browse_history")
public class BrowseHistory {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer articleId;
    private LocalDateTime browseTime;
    private LocalDateTime updateTime;

    // ========== Getters and Setters ==========
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getArticleId() { return articleId; }
    public void setArticleId(Integer articleId) { this.articleId = articleId; }

    public LocalDateTime getBrowseTime() { return browseTime; }
    public void setBrowseTime(LocalDateTime browseTime) { this.browseTime = browseTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}