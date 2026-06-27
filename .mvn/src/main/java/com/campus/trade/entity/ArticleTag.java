package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("article_tag")
public class ArticleTag {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer articleId;
    private Integer tagId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getArticleId() { return articleId; }
    public void setArticleId(Integer articleId) { this.articleId = articleId; }
    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }
}