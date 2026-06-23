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
}