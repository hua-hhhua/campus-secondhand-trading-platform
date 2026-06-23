package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime publishedAt;
    private Integer isTop;
    private Integer allowComment;
    private Integer sendEmail;
    private Integer categoryId;

    // ========== 商品字段 ==========
    private BigDecimal price;
    private Integer stock;
    private Integer schoolId;
    private String location;
    private String coverImage;
    private Integer productStatus;

    // ========== 联系方式 ==========
    private String wechat;
    private String qq;
    private String phone;

    // ========== 标签ID列表（不存数据库） ==========
    @TableField(exist = false)
    private List<Integer> tagIds;

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

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSchoolId() { return schoolId; }
    public void setSchoolId(Integer schoolId) { this.schoolId = schoolId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getProductStatus() { return productStatus; }
    public void setProductStatus(Integer productStatus) { this.productStatus = productStatus; }

    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }

    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<Integer> getTagIds() { return tagIds; }
    public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }
}