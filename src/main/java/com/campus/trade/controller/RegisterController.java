package com.campus.trade.controller;

import com.campus.trade.entity.Result;
import com.campus.trade.entity.User;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
     * 处理注册请求
     */
    @PostMapping("/register")
    @ResponseBody
    public Result register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String nickname,
                           @RequestParam String phone) {
        // 检查用户名是否已存在
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            return Result.error("用户名已存在，请换一个");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 会在 service 中加密
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setRole(0); // 普通用户
        user.setStatus(1); // 正常状态

        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功！请前往登录");
        } else {
            return Result.error("注册失败，请稍后重试");
        }
    }
}