package com.campus.trade.config;

import com.campus.trade.constant.ArticleStatus;
import com.campus.trade.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定时任务配置类
 */
@Component
public class ScheduledTaskConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskConfig.class);

    @Autowired
    private ArticleService articleService;

    /**
     * 定时发布到期的文章
     * 每分钟执行一次，检查状态为定时发布且发布时间已到或已过的文章
     * 使用常量 ArticleStatus.SCHEDULED 替代魔法数字 2
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void publishScheduledArticles() {
        logger.info("【定时任务】开始执行文章定时发布检查 - {}",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            articleService.publishScheduledArticles();
        } catch (Exception e) {
            logger.error("【定时任务】文章定时发布执行失败: {}", e.getMessage());
        }
    }
}