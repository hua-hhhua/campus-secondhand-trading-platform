package com.campus.trade.controller;

import com.campus.trade.entity.User;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 处理注册请求 - 返回 Map（兼容前端）
     */
    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> register(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String nickname,
                                        @RequestParam String phone,
                                        @RequestParam Integer schoolId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(username);
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "用户名已存在，请换一个");
                return result;
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // 会在 service 中加密
            user.setNickname(nickname);
            user.setPhone(phone);
            user.setSchoolId(schoolId);
            user.setRole(0); // 普通用户
            user.setStatus(1); // 正常状态

            boolean success = userService.register(user);
            if (success) {
                result.put("success", true);
                result.put("message", "注册成功！请前往登录");
            } else {
                result.put("success", false);
                result.put("message", "注册失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "注册失败：" + e.getMessage());
        }
        return result;
    }
}