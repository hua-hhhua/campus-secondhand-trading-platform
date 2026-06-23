package com.campus.trade.controller;

import com.campus.trade.entity.ArticleVO;
import com.campus.trade.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ArticleService articleService;

    /**
     * 首页 - 使用JOIN查询显示文章列表（包含作者名和分类名）
     * 方案二：数据库JOIN查询
     */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String keyword) {

        try {
            // 使用JOIN查询获取文章列表（包含作者名和分类名）
            List<ArticleVO> articles = articleService.getPublishedArticleVOsForHome(keyword);
            model.addAttribute("articles", articles != null ? articles : new ArrayList<>());
        } catch (Exception e) {
            System.out.println("首页加载文章列表失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("articles", new ArrayList<>());
        }

        model.addAttribute("keyword", keyword);

        return "index";
    }
}