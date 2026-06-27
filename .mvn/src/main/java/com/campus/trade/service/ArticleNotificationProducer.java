package com.campus.trade.service;

import com.campus.trade.config.RabbitMQConfig;
import com.campus.trade.entity.ArticleNotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleNotificationProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendArticleNotification(ArticleNotificationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ARTICLE_EXCHANGE,
                RabbitMQConfig.ARTICLE_NOTIFICATION_ROUTING_KEY,
                message
        );
        System.out.println("消息已发送: " + message);
    }
}