package com.campus.trade.controller;

import com.campus.trade.entity.Article;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.Comment;
import com.campus.trade.entity.School;
import com.campus.trade.entity.User;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.CategoryService;
import com.campus.trade.service.CommentService;
import com.campus.trade.service.FavoriteService;
import com.campus.trade.service.SchoolService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ArticleDetailController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 商品详情页
     */
    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Integer id, Model model) {
        // 获取商品信息
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
                isAdmin = currentUser.getRole() == 1;
                model.addAttribute("currentUser", currentUser);
            }
        }

        System.out.println("========== 商品详情页调试信息 ==========");
        System.out.println("商品ID: " + id);
        System.out.println("商品标题: " + article.getTitle());
        System.out.println("商品作者ID: " + article.getUserId());
        System.out.println("当前用户ID: " + currentUserId);
        System.out.println("是否管理员: " + isAdmin);

        // 草稿权限控制
        if (article.getStatus() == 0) {
            if (currentUserId == null) {
                return "redirect:/";
            }
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

        // 获取学校名称
        String schoolName = null;
        if (article.getSchoolId() != null) {
            School school = schoolService.getById(article.getSchoolId());
            if (school != null) {
                schoolName = school.getName();
            }
        }

        // 获取分类名称
        String categoryName = null;
        if (article.getCategoryId() != null) {
            Category category = categoryService.getById(article.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 获取评论列表（草稿不显示评论）
        List<Comment> comments = null;
        if (article.getStatus() != 0) {
            comments = commentService.getCommentsByArticleId(id);
        }

        // ========== 判断当前用户是否可以评论 ==========
        boolean canComment = false;
        if (currentUserId != null && article.getStatus() != 0) {
            if (!isAdmin && !currentUserId.equals(article.getUserId())) {
                canComment = true;
            }
        }

        // ========== 判断是否已收藏 ==========
        boolean isFavorited = false;
        if (currentUserId != null) {
            try {
                isFavorited = favoriteService.isFavorited(currentUserId, id);
            } catch (Exception e) {
                isFavorited = false;
            }
        }

        System.out.println("canComment: " + canComment);
        System.out.println("isFavorited: " + isFavorited);
        System.out.println("==========================================");

        model.addAttribute("article", article);
        model.addAttribute("author", author);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("canComment", canComment);
        model.addAttribute("authorName", author != null ? author.getNickname() : "未知");
        model.addAttribute("schoolName", schoolName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("isFavorited", isFavorited);

        return "article-detail";
    }

    /**
     * 发表评论
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

        Article article = articleService.getById(id);
        if (article == null) {
            return "redirect:/";
        }

        if (article.getAllowComment() == 0) {
            return "redirect:/article/" + id + "?error=commentDisabled";
        }

        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole() == 1;

        if (!isAdmin && currentUser.getId().equals(article.getUserId())) {
            System.out.println("【评论拦截】用户不能评论自己发布的文章 - 用户: " + username + ", 文章作者ID: " + article.getUserId());
            return "redirect:/article/" + id + "?error=selfComment";
        }

        if (article.getProductStatus() != 0) {
            return "redirect:/article/" + id + "?error=notForSale";
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