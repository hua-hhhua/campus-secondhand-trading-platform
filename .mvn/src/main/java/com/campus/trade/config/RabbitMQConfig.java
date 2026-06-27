package com.campus.trade.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ARTICLE_EXCHANGE = "article.exchange";
    public static final String ARTICLE_NOTIFICATION_QUEUE = "article.notification.queue";
    public static final String ARTICLE_NOTIFICATION_ROUTING_KEY = "article.notification";

    @Bean
    public TopicExchange articleExchange() {
        return new TopicExchange(ARTICLE_EXCHANGE);
    }

    @Bean
    public Queue articleNotificationQueue() {
        return QueueBuilder.durable(ARTICLE_NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Binding articleNotificationBinding() {
        return BindingBuilder.bind(articleNotificationQueue())
                .to(articleExchange())
                .with(ARTICLE_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        // 配置 ObjectMapper 支持 Java 8 时间类型
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return new Jackson2JsonMessageConverter(objectMapper);
    }
}