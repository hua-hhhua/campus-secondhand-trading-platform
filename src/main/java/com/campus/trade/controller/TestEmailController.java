package com.campus.trade.controller;

import com.campus.trade.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail() {
        try {
            System.out.println("【测试邮件API】开始发送测试邮件...");
            emailService.sendArticleNotificationEmail(
                    "1538292542@qq.com",
                    "测试邮件标题",
                    "这是一封测试邮件，用于验证邮件发送功能是否正常工作。",
                    "测试用户"
            );
            System.out.println("【测试邮件API】发送成功！");
            return "邮件发送成功！请检查收件箱。";
        } catch (Exception e) {
            System.out.println("【测试邮件API】发送失败: " + e.getMessage());
            e.printStackTrace();
            return "邮件发送失败: " + e.getMessage();
        }
    }
}
