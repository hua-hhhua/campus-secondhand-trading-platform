package com.campus.trade.controller;

import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.User;
import com.campus.trade.service.BrowseHistoryService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/browse")
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @Autowired
    private UserService userService;

    /**
     * 我的浏览历史页面
     */
    @GetMapping("/history")
    public String browseHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() instanceof String) {
            return "redirect:/toLoginPage";
        }

        try {
            Object principal = auth.getPrincipal();
            User user = null;
            if (principal instanceof User) {
                user = (User) principal;
            } else {
                String username = auth.getName();
                user = userService.getUserByUsername(username);
            }

            if (user == null) {
                return "redirect:/toLoginPage";
            }

            List<ArticleVO> historyList = browseHistoryService.getBrowseHistoryByUserId(user.getId());
            model.addAttribute("historyList", historyList);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("historyList", new ArrayList<>());
        }

        return "browse-history";
    }

    /**
     * 清空浏览历史
     */
    @DeleteMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearBrowseHistory() {
        Map<String, Object> result = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() instanceof String) {
            result.put("success", false);
            result.put("message", "请先登录");
            result.put("needLogin", true);
            return result;
        }

        try {
            Object principal = auth.getPrincipal();
            User user = null;
            if (principal instanceof User) {
                user = (User) principal;
            } else {
                String username = auth.getName();
                user = userService.getUserByUsername(username);
            }

            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            browseHistoryService.clearBrowseHistory(user.getId());
            result.put("success", true);
            result.put("message", "清空成功");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "清空失败");
        }

        return result;
    }
}