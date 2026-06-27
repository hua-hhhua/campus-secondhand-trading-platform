package com.campus.trade.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ArticleNotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer articleId;
    private String title;
    private String contentSummary;
    private String authorUsername;
    private LocalDateTime publishTime;
    private String notificationType;

    /**
     * 收件人列表（用于邮件通知）
     */
    private List<String> recipients;

    public ArticleNotificationMessage() {}

    public ArticleNotificationMessage(Integer articleId, String title, String contentSummary,
                                      String authorUsername, LocalDateTime publishTime, String notificationType) {
        this.articleId = articleId;
        this.title = title;
        this.contentSummary = contentSummary;
        this.authorUsername = authorUsername;
        this.publishTime = publishTime;
        this.notificationType = notificationType;
    }

    public Integer getArticleId() { return articleId; }
    public void setArticleId(Integer articleId) { this.articleId = articleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentSummary() { return contentSummary; }
    public void setContentSummary(String contentSummary) { this.contentSummary = contentSummary; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }

    @Override
    public String toString() {
        return "ArticleNotificationMessage{" +
                "articleId=" + articleId +
                ", title='" + title + '\'' +
                ", contentSummary='" + contentSummary + '\'' +
                ", authorUsername='" + authorUsername + '\'' +
                ", publishTime=" + publishTime +
                ", notificationType='" + notificationType + '\'' +
                ", recipients=" + recipients +
                '}';
    }
}