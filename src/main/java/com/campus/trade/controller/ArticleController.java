package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.constant.ArticleStatus;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.User;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.AsyncService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private AsyncService asyncService;

    @GetMapping
    public String articlesEntry(
            Model model,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer statusFilter,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");

        // 使用多表查询方法（包含作者名和分类名），支持时间范围查询
        IPage<ArticleVO> articlePage = articleService.getArticleVOPage(page, size, keyword, statusFilter, startTime, endTime);

        model.addAttribute("articlePage", articlePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);

        if (currentUser.getRole() == 1) {
            model.addAttribute("isAdmin", true);
            return "admin/article-manage";
        } else {
            return "admin/my-articles";
        }
    }

    @GetMapping("/form")
    public String articleForm(@RequestParam(required = false) Integer id, Model model, HttpSession session) {
        if (id != null) {
            Article article = articleService.getById(id);
            if (article.getSendEmail() == null) {
                article.setSendEmail(0);
            }
            if (article.getAllowComment() == null) {
                article.setAllowComment(1);
            }
            if (article.getIsTop() == null) {
                article.setIsTop(0);
            }
            model.addAttribute("article", article);
        } else {
            model.addAttribute("article", new Article());
        }
        model.addAttribute("statusDraft", ArticleStatus.DRAFT);
        model.addAttribute("statusPublished", ArticleStatus.PUBLISHED);
        model.addAttribute("statusScheduled", ArticleStatus.SCHEDULED);
        return "admin/article-form";
    }

    @PostMapping("/save")
    public String saveArticle(Article article, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (article.getSendEmail() == null) {
            article.setSendEmail(0);
        }
        if (article.getAllowComment() == null) {
            article.setAllowComment(0);
        }
        if (article.getIsTop() == null) {
            article.setIsTop(0);
        }

        boolean result = articleService.saveOrUpdateArticle(article, currentUser.getId());

        if (result) {
            String statusDesc = "";
            if (article.getStatus() != null) {
                if (article.getStatus() == ArticleStatus.SCHEDULED) {
                    statusDesc = "定时发布";
                } else if (article.getStatus() == ArticleStatus.PUBLISHED) {
                    statusDesc = "发布文章";
                } else {
                    statusDesc = "保存草稿";
                }
            }
            asyncService.asyncLogOperation(currentUser.getUsername(), statusDesc, "文章标题: " + article.getTitle());

            if (article.getStatus() != null && article.getStatus() == ArticleStatus.PUBLISHED) {
                asyncService.asyncUpdateArticleStats(article.getId(), "publish");
            }
        }

        return "redirect:/admin/articles";
    }

    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Article article = articleService.getById(id);

        if (article != null && (currentUser.getRole() == 1 || article.getUserId().equals(currentUser.getId()))) {
            String articleTitle = article.getTitle();
            articleService.deleteArticle(id);
            asyncService.asyncLogOperation(currentUser.getUsername(), "删除文章", "文章ID: " + id + ", 标题: " + articleTitle);
        }
        return "redirect:/admin/articles";
    }
}