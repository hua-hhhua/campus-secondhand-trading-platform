package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.entity.*;
import com.campus.trade.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private SchoolService schoolService

    /**
     * 管理员后台首页
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userService.getUserByUsername(username);
            if (user != null) {
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userNickname", user.getNickname());
                session.setAttribute("userAvatar", user.getAvatar());
                String roleName = (user.getRole() != null && user.getRole() == 1) ? "ADMIN" : "USER";
                session.setAttribute("userRole", roleName);
                model.addAttribute("username", user.getUsername());
                model.addAttribute("userNickname", user.getNickname());
                model.addAttribute("userAvatar", user.getAvatar());
                model.addAttribute("userRole", roleName);
                model.addAttribute("userCount", userService.count());
            }
        }

        model.addAttribute("title", "管理后台");
        return "admin/dashboard";
    }

    // ========== 用户管理 ==========

    @GetMapping("/users")
    public String userManage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer role,
                             @RequestParam(required = false) Integer status,
                             Model model) {
        IPage<User> userPage = userService.getUsersByPage(pageNum, pageSize, keyword, role, status);
        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        return "admin/user-manage";
    }

    @PostMapping("/users/update-status")
    @ResponseBody
    public boolean updateUserStatus(@RequestParam Integer id, @RequestParam Integer status) {
        return userService.updateUserStatus(id, status);
    }

    @PostMapping("/users/update-role")
    @ResponseBody
    public boolean updateUserRole(@RequestParam Integer id, @RequestParam Integer role) {
        return userService.updateUserRole(id, role);
    }

    @PostMapping("/users/delete/{id}")
    @ResponseBody
    public boolean deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id);
    }

    // ========== 分类管理 ==========

    @GetMapping("/categories")
    public String categoryManage(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String keyword,
                                 Model model) {
        IPage<Category> categoryPage = categoryService.getCategoriesByPage(pageNum, pageSize, keyword);
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "admin/category-manage";
    }

    @PostMapping("/categories/add")
    @ResponseBody
    public boolean addCategory(@RequestBody Category category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return categoryService.save(category);
    }

    @PostMapping("/categories/update")
    @ResponseBody
    public boolean updateCategory(@RequestBody Category category) {
        category.setUpdateTime(LocalDateTime.now());
        return categoryService.updateById(category);
    }

    @PostMapping("/categories/delete/{id}")
    @ResponseBody
    public boolean deleteCategory(@PathVariable Integer id) {
        return categoryService.removeById(id);
    }

    @PostMapping("/categories/delete-batch")
    @ResponseBody
    public boolean deleteCategories(@RequestBody List<Integer> ids) {
        return categoryService.removeByIds(ids);
    }

    // ========== 标签管理 ==========

    @GetMapping("/tags")
    public String tagManage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) String keyword,
                            Model model) {
        IPage<Tag> tagPage = tagService.getTagsByPage(pageNum, pageSize, keyword);
        model.addAttribute("tagPage", tagPage);
        model.addAttribute("keyword", keyword);
        return "admin/tag-manage";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public boolean addTag(@RequestBody Tag tag) {
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        return tagService.save(tag);
    }

    @PostMapping("/tags/update")
    @ResponseBody
    public boolean updateTag(@RequestBody Tag tag) {
        tag.setUpdateTime(LocalDateTime.now());
        return tagService.updateById(tag);
    }

    @PostMapping("/tags/delete/{id}")
    @ResponseBody
    public boolean deleteTag(@PathVariable Integer id) {
        return tagService.removeById(id);
    }

    @PostMapping("/tags/delete-batch")
    @ResponseBody
    public boolean deleteTags(@RequestBody List<Integer> ids) {
        return tagService.removeByIds(ids);
    }

    // ========== 评论管理 ==========

    @GetMapping("/comments")
    public String commentManage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Integer articleId,
                                Model model) {
        IPage<Comment> commentPage = commentService.getCommentsByPage(pageNum, pageSize, status, articleId);
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("status", status);
        model.addAttribute("articleId", articleId);
        return "admin/comment-manage";
    }

    @PostMapping("/comments/approve/{id}")
    @ResponseBody
    public boolean approveComment(@PathVariable Integer id) {
        return commentService.approveComment(id);
    }

    @PostMapping("/comments/reject/{id}")
    @ResponseBody
    public boolean rejectComment(@PathVariable Integer id) {
        return commentService.rejectComment(id);
    }

    @PostMapping("/comments/delete/{id}")
    @ResponseBody
    public boolean deleteComment(@PathVariable Integer id) {
        return commentService.deleteComment(id, null);
    }

    @PostMapping("/comments/delete-batch")
    @ResponseBody
    public boolean deleteComments(@RequestBody List<Integer> ids) {
        return commentService.deleteComments(ids);
    }

    // ========== 商品管理 ==========

    @GetMapping("/articles")
    public String articleManage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer statusFilter,
                                Model model) {
        IPage<Article> articlePage = articleService.findAllPage(pageNum, pageSize, keyword);
        model.addAttribute("articlePage", articlePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        return "admin/article-manage";
    }

    @PostMapping("/articles/update-status")
    @ResponseBody
    public boolean updateArticleStatus(@RequestParam Integer id, @RequestParam Integer status) {
        Article article = articleService.getById(id);
        if (article != null) {
            article.setStatus(status);
            article.setUpdateTime(LocalDateTime.now());
            return articleService.updateById(article);
        }
        return false;
    }

    @PostMapping("/articles/delete/{id}")
    @ResponseBody
    public boolean deleteArticle(@PathVariable Integer id) {
        return articleService.deleteArticle(id);
    }

    @PostMapping("/articles/delete-batch")
    @ResponseBody
    public boolean deleteArticles(@RequestBody List<Integer> ids) {
        return articleService.removeByIds(ids);
    }

    // ========== 学校管理 ==========

    @GetMapping("/schools")
    public String schoolManage(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        IPage<School> schoolPage = schoolService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize),
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<School>()
                        .like(keyword != null && !keyword.isEmpty(), "name", keyword)
                        .or()
                        .like(keyword != null && !keyword.isEmpty(), "city", keyword));
        model.addAttribute("schoolPage", schoolPage);
        model.addAttribute("keyword", keyword);
        return "admin/school-manage";
    }

    @PostMapping("/schools/add")
    @ResponseBody
    public boolean addSchool(@RequestBody School school) {
        school.setCreateTime(LocalDateTime.now());
        school.setUpdateTime(LocalDateTime.now());
        return schoolService.save(school);
    }

    @PostMapping("/schools/update")
    @ResponseBody
    public boolean updateSchool(@RequestBody School school) {
        school.setUpdateTime(LocalDateTime.now());
        return schoolService.updateById(school);
    }

    @PostMapping("/schools/delete/{id}")
    @ResponseBody
    public boolean deleteSchool(@PathVariable Integer id) {
        return schoolService.removeById(id);
    }

    @PostMapping("/schools/delete-batch")
    @ResponseBody
    public boolean deleteSchools(@RequestBody List<Integer> ids) {
        return schoolService.removeByIds(ids);
    }

}