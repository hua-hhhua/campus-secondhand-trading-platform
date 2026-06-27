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

        if (disabled != null) {
            model.addAttribute("errorMsg", "您的账号已被管理员禁用，请联系管理员恢复");
            session.invalidate();
            return "login";
        }

        if (error != null) {
            String username = (String) session.getAttribute("loginErrorUsername");

            // 检查是否已被删除
            Boolean isDeleted = (Boolean) session.getAttribute("loginErrorDeleted");
            if (isDeleted != null && isDeleted) {
                model.addAttribute("errorMsg", "您的账户因涉嫌违规已被删除，请联系管理员");
                session.removeAttribute("loginErrorDeleted");
                session.removeAttribute("loginErrorUsername");
                return "login";
            }

            if (username != null) {
                try {
                    User user = userService.findByUsername(username);
                    if (user == null) {
                        model.addAttribute("errorMsg", "您的账户因涉嫌违规已被删除，请联系管理员");
                        session.removeAttribute("loginErrorUsername");
                        return "login";
                    }
                    if (user.getStatus() == 0) {
                        model.addAttribute("errorMsg", "您的账号已被管理员禁用，请联系管理员恢复");
                        session.removeAttribute("loginErrorUsername");
                        return "login";
                    }
                } catch (Exception e) {
                    // 查询失败，使用默认提示
                }
            }
            model.addAttribute("errorMsg", "用户名或密码错误！");
            session.removeAttribute("loginErrorUsername");
            session.removeAttribute("loginErrorDisabled");
            session.removeAttribute("loginErrorDeleted");
        }

        if (logout != null) {
            model.addAttribute("logoutMsg", "已成功退出登录！");
        }

        return "login";
    }
}