package com.campus.trade.service;

import com.campus.trade.entity.ArticleNotificationMessage;
import com.campus.trade.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.Arrays;
import java.util.List;

@Service
public class ArticleNotificationConsumer {

    @Autowired
    private EmailService emailService;

    /**
     * 默认通知邮箱列表，从配置文件读取
     */
    @Value("${blog.notification.emails:admin@example.com}")
    private String defaultNotificationEmails;

    @RabbitListener(queues = "article.notification.queue")
    public void handleArticleNotification(ArticleNotificationMessage message) {
        System.out.println("收到文章通知消息: " + message);

        try {
            switch (message.getNotificationType()) {
                case "publish":
                    System.out.println("处理文章发布通知: " + message.getTitle());
                    // 发送邮件通知
                    sendArticleNotificationEmails(message);
                    break;
                case "update":
                    System.out.println("处理文章更新通知: " + message.getTitle());
                    // 可选：发送更新通知邮件
                    break;
                case "delete":
                    System.out.println("处理文章删除通知: " + message.getTitle());
                    // 可选：发送删除通知邮件
                    break;
                default:
                    System.out.println("未知通知类型: " + message.getNotificationType());
            }
        } catch (Exception e) {
            System.err.println("处理文章通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送文章通知邮件
     */
    private void sendArticleNotificationEmails(ArticleNotificationMessage message) {
        try {
            // 获取收件人列表
            List<String> recipients = message.getRecipients();
            if (recipients == null || recipients.isEmpty()) {
                // 使用默认通知邮箱
                String[] emails = defaultNotificationEmails.split(",");
                recipients = Arrays.asList(emails);
            }

            // 为每个收件人发送邮件
            for (String recipient : recipients) {
                if (recipient != null && !recipient.trim().isEmpty()) {
                    try {
                        emailService.sendArticleNotificationEmail(
                                recipient.trim(),
                                message.getTitle(),
                                message.getContentSummary(),
                                message.getAuthorUsername()
                        );
                        System.out.println("✅ 邮件已发送至: " + recipient);
                    } catch (Exception e) {
                        System.err.println("❌ 发送至 " + recipient + " 失败: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("发送邮件通知失败: " + e.getMessage());
            // 邮件发送失败不影响主流程，只记录日志
        }
    }
}