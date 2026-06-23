package com.campus.trade.controller;

import com.campus.trade.entity.Article;
import com.campus.trade.entity.Comment;
import com.campus.trade.entity.User;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.CommentService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ArticleDetailController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    /**
     * 文章详情页
     */
    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Integer id, Model model) {
        // 获取文章信息
        Article article = articleService.getById(id);
        if (article == null) {
            return "redirect:/";
        }

        // 获取当前登录用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;
        Integer currentUserId = null;
        boolean isAdmin = false;

        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            currentUsername = auth.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            if (currentUser != null) {
                currentUserId = currentUser.getId();
                isAdmin = currentUser.getRole() == 1; // role=1 是管理员
                model.addAttribute("currentUser", currentUser);
            }
        }

        // ========== 草稿权限控制 ==========
        // 状态 0 表示草稿
        if (article.getStatus() == 0) {
            // 未登录用户不能查看草稿
            if (currentUserId == null) {
                return "redirect:/";
            }
            // 只有作者本人或管理员可以查看草稿
            if (!isAdmin && !article.getUserId().equals(currentUserId)) {
                return "redirect:/";
            }
        }

        // 增加浏览量（草稿不增加浏览量）
        if (article.getStatus() != 0) {
            articleService.incrementViewCount(id);
        }

        // 获取文章作者信息
        User author = userService.getById(article.getUserId());

        // 获取评论列表（草稿不显示评论）
        List<Comment> comments = null;
        if (article.getStatus() != 0) {
            comments = commentService.getCommentsByArticleId(id);
        }

        // ========== 判断当前用户是否可以评论 ==========
        // 规则：用户不能评论自己发布的文章，但可以查看自己文章的评论
        // 条件：已登录 + 不是管理员 + 不是文章作者 → 可以评论
        boolean canComment = false;
        if (currentUserId != null && article.getStatus() != 0) {
            // 非管理员且不是文章作者才能评论
            if (!isAdmin && !currentUserId.equals(article.getUserId())) {
                canComment = true;
            }
        }

        model.addAttribute("article", article);
        model.addAttribute("author", author);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("canComment", canComment);

        return "article-detail";
    }

    /**
     * 发表评论
     * 增加校验：用户不能评论自己发布的文章
     */
    @PostMapping("/article/{id}/comment")
    public String addComment(@PathVariable Integer id,
                             @RequestParam String content,
                             @RequestParam(required = false) Integer parentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() instanceof String) {
            return "redirect:/toLoginPage";
        }

        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        // ========== 获取文章信息，检查是否是作者自己评论 ==========
        Article article = articleService.getById(id);
        if (article == null) {
            return "redirect:/";
        }

        // 判断是否为管理员
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole() == 1;

        // ========== 用户不能评论自己发布的文章（管理员除外） ==========
        if (!isAdmin && currentUser.getId().equals(article.getUserId())) {
            // 不能评论自己的文章，重定向回详情页，不保存评论
            System.out.println("【评论拦截】用户不能评论自己发布的文章 - 用户: " + username + ", 文章作者ID: " + article.getUserId());
            return "redirect:/article/" + id + "?error=selfComment";
        }

        Comment comment = new Comment();
        comment.setArticleId(id);
        comment.setUserId(currentUser.getId());
        comment.setContent(content);
        comment.setParentId(parentId != null ? parentId : 0);

        commentService.addComment(comment);

        return "redirect:/article/" + id;
    }
}