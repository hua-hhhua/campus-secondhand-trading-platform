package com.campus.trade.controller;

import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.School;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.CategoryService;
import com.campus.trade.service.SchoolService;
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

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 首页 - 多条件查询（关键词 + 学校 + 分类）
     */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer schoolId,
                        @RequestParam(required = false) Integer categoryId) {

        System.out.println("========== 首页搜索参数 ==========");
        System.out.println("关键词: " + keyword);
        System.out.println("学校ID: " + schoolId);
        System.out.println("分类ID: " + categoryId);

        try {
            // 获取所有学校和分类（用于筛选下拉框）
            List<School> schools = schoolService.list();
            List<Category> categories = categoryService.list();

            // 多条件查询商品
            List<ArticleVO> articles = articleService.getArticlesByConditions(keyword, schoolId, categoryId);

            System.out.println("查询到商品数量: " + (articles != null ? articles.size() : 0));

            model.addAttribute("articles", articles != null ? articles : new ArrayList<>());
            model.addAttribute("schools", schools);
            model.addAttribute("categories", categories);
            model.addAttribute("keyword", keyword);
            model.addAttribute("schoolId", schoolId);
            model.addAttribute("categoryId", categoryId);

        } catch (Exception e) {
            System.out.println("首页加载商品列表失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("articles", new ArrayList<>());
            model.addAttribute("schools", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
        }

        return "index";
    }
}