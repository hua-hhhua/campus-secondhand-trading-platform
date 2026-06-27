package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.User;
import com.campus.trade.service.FavoriteService;
import com.campus.trade.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggle(@RequestParam Integer articleId) {
        Map<String, Object> result = new HashMap<>();

        logger.info("========== 收到收藏请求 ==========");
        logger.info("articleId: {}", articleId);

        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication: {}", auth);

        if (auth == null) {
            logger.warn("Authentication 为 null");
            result.put("success", false);
            result.put("message", "请先登录");
            result.put("needLogin", true);
            return result;
        }

        Object principal = auth.getPrincipal();
        logger.info("Principal: {}, 类型: {}", principal, principal != null ? principal.getClass().getName() : "null");

        if (principal instanceof String && "anonymousUser".equals(principal)) {
            logger.warn("用户未登录（anonymousUser）");
            result.put("success", false);
            result.put("message", "请先登录");
            result.put("needLogin", true);
            return result;
        }

        try {
            User user = null;
            if (principal instanceof User) {
                user = (User) principal;
                logger.info("从 Principal 直接获取到 User: id={}, username={}", user.getId(), user.getUsername());
            } else {
                // 通过用户名查询
                String username = auth.getName();
                logger.info("用户名: {}", username);
                user = userService.getUserByUsername(username);
                logger.info("通过用户名查询到 User: {}", user);
            }

            if (user == null) {
                logger.error("用户不存在");
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            logger.info("执行切换收藏: userId={}, articleId={}", user.getId(), articleId);
            boolean favorited = favoriteService.toggleFavorite(user.getId(), articleId);
            logger.info("收藏结果: {}", favorited);

            int count = favoriteService.getFavoriteCount(articleId);
            logger.info("收藏数量: {}", count);

            result.put("success", true);
            result.put("favorited", favorited);
            result.put("count", count);
            result.put("message", favorited ? "收藏成功" : "已取消收藏");
        } catch (Exception e) {
            logger.error("收藏操作异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    @ResponseBody
    public Map<String, Object> check(@RequestParam Integer articleId) {
        Map<String, Object> result = new HashMap<>();

        logger.info("【检查收藏】articleId: {}", articleId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() instanceof String) {
            result.put("isFavorited", false);
            result.put("count", favoriteService.getFavoriteCount(articleId));
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

            if (user != null) {
                boolean isFavorited = favoriteService.isFavorited(user.getId(), articleId);
                int count = favoriteService.getFavoriteCount(articleId);
                result.put("isFavorited", isFavorited);
                result.put("count", count);
                logger.info("【检查收藏】userId: {}, isFavorited: {}, count: {}", user.getId(), isFavorited, count);
            } else {
                result.put("isFavorited", false);
                result.put("count", favoriteService.getFavoriteCount(articleId));
            }
        } catch (Exception e) {
            logger.error("【检查收藏】异常：", e);
            result.put("isFavorited", false);
            result.put("count", 0);
        }

        return result;
    }

    /**
     * 我的收藏页面
     */
    @GetMapping("/my")
    public String myFavorites(Model model,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        logger.info("【我的收藏】进入页面，page: {}, size: {}", page, size);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() instanceof String) {
            logger.warn("【我的收藏】用户未登录，跳转到登录页");
            return "redirect:/toLoginPage";
        }

        try {
            Object principal = auth.getPrincipal();
            User user = null;
            if (principal instanceof User) {
                user = (User) principal;
                logger.info("【我的收藏】从 Principal 获取到 User: id={}", user.getId());
            } else {
                String username = auth.getName();
                user = userService.getUserByUsername(username);
                logger.info("【我的收藏】从用户名查询到 User: id={}", user != null ? user.getId() : "null");
            }

            if (user == null) {
                logger.warn("【我的收藏】用户不存在");
                return "redirect:/toLoginPage";
            }

            IPage<ArticleVO> favorites = favoriteService.getUserFavorites(user.getId(), page, size);
            logger.info("【我的收藏】查询结果: total={}, records={}", favorites.getTotal(), favorites.getRecords().size());

            model.addAttribute("favorites", favorites.getRecords());
            model.addAttribute("favoritePage", favorites);
        } catch (Exception e) {
            logger.error("【我的收藏】异常：", e);
            model.addAttribute("favorites", new ArrayList<>());
            model.addAttribute("favoritePage", null);
        }

        return "my-favorites";
    }
}