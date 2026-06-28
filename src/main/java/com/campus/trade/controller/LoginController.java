package com.campus.trade.controller;

import com.campus.trade.entity.User;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Calendar;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/toLoginPage")
    public String toLoginPage(
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "disabled", required = false) String disabled,
            HttpSession session) {

        model.addAttribute("currentYear", Calendar.getInstance().get(Calendar.YEAR));

        // 处理被禁用的用户（直接参数）
        if (disabled != null) {
            model.addAttribute("errorMsg", "您的账号已被管理员禁用，请联系管理员恢复");
            session.invalidate();
            return "login";
        }

        // 处理登录失败
        if (error != null) {
            // 从session中获取登录失败时的用户名
            String username = (String) session.getAttribute("loginErrorUsername");

            if (username != null) {
                try {
                    User user = userService.findByUsername(username);
                    if (user != null && user.getStatus() == 0) {
                        model.addAttribute("errorMsg", "您的账号已被管理员禁用，请联系管理员恢复");
                        session.removeAttribute("loginErrorUsername");
                        session.removeAttribute("loginErrorDisabled");
                        return "login";
                    }
                } catch (Exception e) {
                    // 查询失败，使用默认提示
                }
            }
            model.addAttribute("errorMsg", "用户名或密码错误！");
            session.removeAttribute("loginErrorUsername");
            session.removeAttribute("loginErrorDisabled");
        }

        if (logout != null) {
            model.addAttribute("logoutMsg", "已成功退出登录！");
        }

        return "login";
    }
}