package com.campus.trade.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param text 内容
     */
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * 发送 HTML 格式邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML 内容
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * 发送文章发布通知邮件
     * @param to 收件人
     * @param articleTitle 文章标题
     * @param articleSummary 文章摘要
     * @param author 作者
     */
    void sendArticleNotificationEmail(String to, String articleTitle, String articleSummary, String author);
}