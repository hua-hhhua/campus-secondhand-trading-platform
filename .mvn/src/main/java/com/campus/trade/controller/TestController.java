package com.campus.trade.controller;

import com.campus.trade.entity.ArticleNotificationMessage;
import com.campus.trade.service.ArticleNotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private ArticleNotificationProducer articleNotificationProducer;

    @PostMapping("/send-article-notification")
    public ResponseEntity<Map<String, Object>> sendTestNotification(@RequestBody ArticleNotificationMessage message) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (message.getPublishTime() == null) {
                message.setPublishTime(LocalDateTime.now());
            }
            if (message.getNotificationType() == null) {
                message.setNotificationType("publish");
            }
            articleNotificationProducer.sendArticleNotification(message);
            response.put("success", true);
            response.put("message", "消息发送成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "消息发送失败：" + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}