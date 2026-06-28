package com.campus.trade.service;

import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleNotificationMessage;
import com.campus.trade.service.ArticleNotificationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AsyncService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
    private ArticleNotificationProducer articleNotificationProducer;

    @Autowired
    private EmailService emailService;

    @Value("${blog.notification.emails:admin@example.com}")
    private String defaultNotificationEmails;

    /**
     * 异步记录操作日志（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncLogOperation(String username, String operation, String details) {
        try {
            logger.info("【异步任务开始】异步日志记录 - 用户: {}, 操作: {}, 开始时间: {}", username, operation, System.currentTimeMillis());
            Thread.sleep(2000);  // 模拟耗时2秒
            logger.info("【异步任务完成】异步日志记录 - 用户: {}, 操作: {}, 详情: {}", username, operation, details);
        } catch (Exception e) {
            logger.error("异步日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 异步发送通知（邮件/站内信）（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncSendNotification(String targetUser, String title, String content) {
        try {
            logger.info("【异步任务开始】异步发送通知 - 目标: {}, 标题: {}, 开始时间: {}", targetUser, title, System.currentTimeMillis());
            Thread.sleep(2000);  // 模拟耗时2秒
            logger.info("【异步任务完成】异步发送通知 - 目标: {}, 标题: {}, 内容: {}", targetUser, title, content);
        } catch (Exception e) {
            logger.error("异步发送通知失败: {}", e.getMessage());
        }
    }

    /**
     * 异步更新文章统计（阅读量、点赞量等）（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncUpdateArticleStats(Integer articleId, String statsType) {
        try {
            logger.info("【异步任务开始】异步更新文章统计 - 文章ID: {}, 统计类型: {}, 开始时间: {}", articleId, statsType, System.currentTimeMillis());
            Thread.sleep(2000);  // 模拟耗时2秒
            logger.info("【异步任务完成】异步更新文章统计 - 文章ID: {}, 统计类型: {}", articleId, statsType);
        } catch (Exception e) {
            logger.error("异步更新文章统计失败: {}", e.getMessage());
        }
    }

    /**
     * 异步发送文章通知（通过MQ）（耗时3秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncSendArticleNotification(Article article, String type) {
        try {
            logger.info("【异步任务开始】异步发送文章通知 - 文章ID: {}, 类型: {}, 开始时间: {}", article.getId(), type, System.currentTimeMillis());

            if ("publish".equals(type)) {
                sendNotificationEmailsDirectly(article);
            }

            ArticleNotificationMessage notification = new ArticleNotificationMessage();
            notification.setArticleId(article.getId());
            notification.setTitle(article.getTitle());
            notification.setContentSummary(getSummary(article.getContent()));
            notification.setAuthorUsername(getCurrentUsername());
            notification.setPublishTime(article.getCreateTime());
            notification.setNotificationType(type);
            articleNotificationProducer.sendArticleNotification(notification);
            logger.info("【异步任务完成】异步发送文章通知 - 文章ID: {}, 类型: {}", article.getId(), type);
        } catch (Exception e) {
            logger.error("异步发送文章通知失败: {}", e.getMessage());
        }
    }

    /**
     * 直接发送邮件通知（不依赖 RabbitMQ）
     */
    private void sendNotificationEmailsDirectly(Article article) {
        try {
            String[] emails = defaultNotificationEmails.split(",");
            List<String> recipients = Arrays.asList(emails);
            String author = article.getUserId() != null ? "用户ID:" + article.getUserId() : "匿名用户";

            for (String recipient : recipients) {
                if (recipient != null && !recipient.trim().isEmpty()) {
                    try {
                        emailService.sendArticleNotificationEmail(
                                recipient.trim(),
                                article.getTitle(),
                                getSummary(article.getContent()),
                                author
                        );
                        logger.info("✅ 邮件已直接发送至: {}", recipient);
                    } catch (Exception e) {
                        logger.error("❌ 发送至 {} 失败: {}", recipient, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("发送邮件通知失败: {}", e.getMessage());
        }
    }

    /**
     * 异步发送删除文章通知（通过MQ）（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncSendDeleteNotification(Integer articleId, String title, String username) {
        try {
            logger.info("【异步任务开始】异步发送删除文章通知 - 文章ID: {}, 标题: {}, 开始时间: {}", articleId, title, System.currentTimeMillis());
            Thread.sleep(2000);  // 模拟耗时2秒

            ArticleNotificationMessage notification = new ArticleNotificationMessage();
            notification.setArticleId(articleId);
            notification.setTitle(title);
            notification.setAuthorUsername(username);
            notification.setPublishTime(java.time.LocalDateTime.now());
            notification.setNotificationType("delete");
            articleNotificationProducer.sendArticleNotification(notification);
            logger.info("【异步任务完成】异步发送删除文章通知 - 文章ID: {}, 标题: {}", articleId, title);
        } catch (Exception e) {
            logger.error("异步发送删除文章通知失败: {}", e.getMessage());
        }
    }

    /**
     * 异步压缩图片（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncCompressImage(String sourcePath, String targetPath) {
        try {
            if (sourcePath.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                logger.info("【异步任务开始】异步压缩图片 - 源文件: {}, 开始时间: {}", sourcePath, System.currentTimeMillis());
                Thread.sleep(2000);  // 模拟耗时2秒
                logger.info("【异步任务完成】异步压缩图片 - 源文件: {}, 目标文件: {}", sourcePath, targetPath);
            }
        } catch (Exception e) {
            logger.error("异步压缩图片失败: {}", e.getMessage());
        }
    }

    /**
     * 异步压缩图片（带计时，用于演示主线程和异步线程耗时对比）
     */
    @Async("asyncTaskExecutor")
    public void asyncCompressImageWithTiming(String sourcePath, String targetPath) {
        long startTime = System.currentTimeMillis();
        try {
            if (sourcePath.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                // 模拟耗时压缩操作
                Thread.sleep(2000);

                // 实际压缩代码（可选，取消注释即可启用）
                // cn.hutool.core.img.ImgUtil.scale(
                //     new java.io.File(sourcePath),
                //     new java.io.File(targetPath),
                //     0.5f
                // );

                long endTime = System.currentTimeMillis();
                long cost = endTime - startTime;
                System.out.println("【异步任务】异步压缩图片成功 - 源文件: " + sourcePath + "，耗时：" + cost + "毫秒");
                logger.info("异步压缩图片成功 - 源文件: {}, 目标文件: {}, 耗时: {}毫秒", sourcePath, targetPath, cost);
            }
        } catch (Exception e) {
            logger.error("异步压缩图片失败: {}", e.getMessage());
        }
    }

    /**
     * 异步初始化用户数据（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncInitUserData(Integer userId, String username) {
        try {
            logger.info("【异步任务开始】异步初始化用户数据 - 用户ID: {}, 用户名: {}, 开始时间: {}", userId, username, System.currentTimeMillis());
            Thread.sleep(2000);  // 模拟耗时2秒
            logger.info("【异步任务完成】异步初始化用户数据 - 用户ID: {}, 用户名: {}", userId, username);
        } catch (Exception e) {
            logger.error("异步初始化用户数据失败: {}", e.getMessage());
        }
    }

    /**
     * 异步保存物理文件（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncSaveFile(byte[] fileBytes, String filePath) {
        try {
            logger.info("【异步任务开始】异步保存文件 - 路径: {}, 开始时间: {}", filePath, System.currentTimeMillis());
            Thread.sleep(2000);  // 模拟耗时2秒

            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            cn.hutool.core.io.FileUtil.mkdir(path.getParent().toString());
            java.nio.file.Files.write(path, fileBytes);
            logger.info("【异步任务完成】异步保存文件成功 - 路径: {}", filePath);
        } catch (Exception e) {
            logger.error("异步保存文件失败: {}", e.getMessage());
        }
    }

    /**
     * 异步生成缩略图（耗时2秒）
     */
    @Async("asyncTaskExecutor")
    public void asyncGenerateThumbnail(String sourcePath, String thumbnailPath, float scale) {
        try {
            if (sourcePath.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                logger.info("【异步任务开始】异步生成缩略图 - 源文件: {}, 开始时间: {}", sourcePath, System.currentTimeMillis());
                Thread.sleep(2000);  // 模拟耗时2秒

                cn.hutool.core.img.ImgUtil.scale(
                        new java.io.File(sourcePath),
                        new java.io.File(thumbnailPath),
                        scale
                );
                logger.info("【异步任务完成】异步生成缩略图成功 - 源文件: {}, 缩略图: {}", sourcePath, thumbnailPath);
            }
        } catch (Exception e) {
            logger.error("异步生成缩略图失败: {}", e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    private String getSummary(String content) {
        if (content == null) return "";
        return content.length() > 200 ? content.substring(0, 200) : content;
    }

    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception e) {
            // 忽略
        }
        return "system";
    }
}