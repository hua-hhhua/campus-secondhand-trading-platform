package com.campus.trade.service.impl;

import com.campus.trade.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("简单邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (Exception e) {
            logger.error("简单邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("HTML邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            logger.error("HTML邮件发送失败 - 收件人: {}, 错误: {}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    @Override
    public void sendArticleNotificationEmail(String to, String articleTitle, String articleSummary, String author) {
        String subject = "【校园交易平台】新文章发布通知 - " + articleTitle;

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<style>")
                .append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }")
                .append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }")
                .append(".header h1 { margin: 0; font-size: 24px; }")
                .append(".content { padding: 30px; }")
                .append(".article-title { font-size: 20px; color: #333; margin-bottom: 15px; font-weight: bold; }")
                .append(".article-summary { color: #666; line-height: 1.8; margin-bottom: 20px; }")
                .append(".author-info { color: #999; font-size: 14px; margin-bottom: 20px; }")
                .append(".button { display: inline-block; padding: 12px 30px; background-color: #667eea; color: white; text-decoration: none; border-radius: 5px; margin-top: 10px; }")
                .append(".footer { background-color: #f9f9f9; padding: 20px; text-align: center; color: #999; font-size: 12px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h1>📰 新文章发布</h1>")
                .append("</div>")
                .append("<div class='content'>")
                .append("<div class='article-title'>").append(articleTitle).append("</div>")
                .append("<div class='author-info'>作者：").append(author).append("</div>")
                .append("<div class='article-summary'>")
                .append("<strong>文章摘要：</strong><br>")
                .append(articleSummary != null ? articleSummary : "暂无摘要")
                .append("</div>")
                .append("<a href='http://localhost:8080/' class='button'>查看文章</a>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>此邮件由校园交易平台自动发送，请勿回复</p>")
                .append("<p>&copy; 2026 校园交易平台</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        sendHtmlEmail(to, subject, htmlContent.toString());
    }
}