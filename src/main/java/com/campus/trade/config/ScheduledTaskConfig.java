package com.campus.trade.config;

import com.campus.trade.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduledTaskConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskConfig.class);
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ArticleService articleService;

    @Scheduled(cron = "0 * * * * ?")
    public void publishScheduledArticles() {
        LocalDateTime now = LocalDateTime.now(SHANGHAI_ZONE);
        logger.info("【定时任务】开始执行文章定时发布检查 - {}", now.format(FORMATTER));

        try {
            articleService.publishScheduledArticles(now);
            logger.info("【定时任务】文章定时发布检查完成 - {}", now.format(FORMATTER));
        } catch (Exception e) {
            logger.error("【定时任务】文章定时发布执行失败", e);
        }
    }
}