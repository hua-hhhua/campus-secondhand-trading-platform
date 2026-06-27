package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private TagService tagService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private com.campus.trade.mapper.OrderReviewMapper orderReviewMapper;


    // ============================================================
    //  1. 订单管理
    // ============================================================

    @GetMapping("/orders")
    public String orderManage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer statusFilter,
                              Model model) {
        IPage<Order> orderPage = orderService.page(
                new Page<>(pageNum, pageSize),
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .like(keyword != null && !keyword.isEmpty(), "order_no", keyword)
                        .or()
                        .like(keyword != null && !keyword.isEmpty(), "buyer_name", keyword)
                        .or()
                        .like(keyword != null && !keyword.isEmpty(), "seller_name", keyword)
                        .eq(statusFilter != null, "status", statusFilter)
                        .orderByDesc("created_at")
        );
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        return "admin/order-manage";
    }

    @GetMapping("/orders/{id}")
    @ResponseBody
    public Order getOrderDetail(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @PostMapping("/orders/status/{id}")
    @ResponseBody
    public boolean updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }

    @PostMapping("/orders/update-status")
    @ResponseBody
    public boolean updateOrderStatusByParam(@RequestParam Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }

    @PostMapping("/orders/delete/{id}")
    @ResponseBody
    public boolean deleteOrder(@PathVariable Long id) {
        return orderService.adminDeleteOrder(id);
    }

    @PostMapping("/orders/delete-batch")
    @ResponseBody
    public boolean deleteOrders(@RequestBody List<Long> ids) {
        return orderService.removeByIds(ids);
    }

    // ============================================================
    //  2. 用户管理
    // ============================================================

    @GetMapping("/users")
    public String userManage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String startTime,
                             @RequestParam(required = false) String endTime,
                             Model model) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getPhone, keyword);
        }
        // 时间范围查询
        if (startTime != null && !startTime.isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime + "T00:00:00");
                wrapper.ge(User::getCreatedAt, start);
            } catch (Exception e) {
                // 忽略格式错误
            }
        }
        if (endTime != null && !endTime.isEmpty()) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime + "T23:59:59");
                wrapper.le(User::getCreatedAt, end);
            } catch (Exception e) {
                // 忽略格式错误
            }
        }
        wrapper.orderByDesc(User::getCreatedAt);
        IPage<User> userPage = userService.page(page, wrapper);
        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        return "admin/user-manage";
    }

    @GetMapping("/users/add")
    public String addUserPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserPage(@PathVariable Integer id, Model model) {
        User user = userService.getById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(User user) {
        if (user.getId() == null) {
            user.setRole(0);
            user.setStatus(1);
            userService.save(user);
        } else {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                User existing = userService.getById(user.getId());
                if (existing != null) {
                    user.setPassword(existing.getPassword());
                }
            }
            userService.updateById(user);
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.removeById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/batchDelete")
    public String batchDeleteUsers(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            userService.removeById(Integer.parseInt(id));
        }
        return "redirect:/admin/users";
    }

    // ============================================================
    //  3. 文章管理（使用联表查询）
    // ============================================================

    @GetMapping("/articles")
    public String articleManage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer statusFilter,
                                @RequestParam(required = false) Integer categoryId,
                                @RequestParam(required = false) String startTime,
                                @RequestParam(required = false) String endTime,
                                Model model) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("a.title", keyword)
                    .or()
                    .like("a.content", keyword);
        }
        if (statusFilter != null) {
            wrapper.eq("a.status", statusFilter);
        }
        if (categoryId != null) {
            wrapper.eq("a.category_id", categoryId);
        }

        // 时间范围查询
        if (startTime != null && !startTime.isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime + "T00:00:00");
                wrapper.ge("a.create_time", start);
            } catch (Exception e) {
                // 忽略格式错误
            }
        }
        if (endTime != null && !endTime.isEmpty()) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime + "T23:59:59");
                wrapper.le("a.create_time", end);
            } catch (Exception e) {
                // 忽略格式错误
            }
        }

        wrapper.orderByDesc("a.is_top")
                .orderByDesc("a.create_time");

        // 使用联表查询，直接填充 authorName 和 categoryName
        IPage<Article> articlePage = articleMapper.selectArticlePageWithInfo(page, wrapper);

        model.addAttribute("articlePage", articlePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        model.addAttribute("categories", categoryService.list());
        return "admin/article-manage";
    }

    @GetMapping("/articles/form")
    public String articleForm(@RequestParam(required = false) Integer id, Model model) {
        Article article;
        boolean isEdit = false;
        if (id != null) {
            article = articleService.getById(id);
            isEdit = true;
        } else {
            article = new Article();
        }
        model.addAttribute("article", article);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("categories", categoryService.list());
        model.addAttribute("schools", schoolService.list());
        return "admin/article-form";
    }

    @PostMapping("/articles/save")
    public String saveArticle(Article article) {
        if (article.getId() == null) {
            article.setStatus(1);
            article.setViewCount(0);
            article.setIsTop(0);
            article.setAllowComment(1);
            article.setSendEmail(0);
            article.setCreateTime(LocalDateTime.now());
            article.setUpdateTime(LocalDateTime.now());
            articleService.save(article);
        } else {
            article.setUpdateTime(LocalDateTime.now());
            articleService.updateById(article);
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable Integer id) {
        articleService.removeById(id);
        return "redirect:/admin/articles";
    }

    @GetMapping("/articles/batchDelete")
    public String batchDeleteArticles(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            articleService.removeById(Integer.parseInt(id));
        }
        return "redirect:/admin/articles";
    }

    @PostMapping("/articles/status/{id}")
    @ResponseBody
    public boolean updateArticleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(status);
        article.setUpdateTime(LocalDateTime.now());
        return articleService.updateById(article);
    }

    // ============================================================
    //  4. 分类管理
    // ============================================================

    @GetMapping("/categories")
    public String categoryManage(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String keyword,
                                 Model model) {
        IPage<Category> categoryPage = categoryService.getCategoriesByPage(pageNum, pageSize, keyword);
        List<Category> allCategories = categoryService.getAllCategories();
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("keyword", keyword);
        return "admin/category-manage";
    }

    @PostMapping("/categories/add")
    @ResponseBody
    public boolean addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    @PostMapping("/categories/update")
    @ResponseBody
    public boolean updateCategory(@RequestBody Category category) {
        return categoryService.updateCategory(category);
    }

    @PostMapping("/categories/delete/{id}")
    @ResponseBody
    public boolean deleteCategory(@PathVariable Integer id) {
        return categoryService.deleteCategory(id);
    }

    @PostMapping("/categories/delete-batch")
    @ResponseBody
    public boolean deleteCategories(@RequestBody List<Integer> ids) {
        return categoryService.deleteCategories(ids);
    }

    // ============================================================
    //  5. 学校管理
    // ============================================================

    @GetMapping("/schools")
    public String schoolManage(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        IPage<School> schoolPage = schoolService.getSchoolsByPage(pageNum, pageSize, keyword);
        model.addAttribute("schoolPage", schoolPage);
        model.addAttribute("keyword", keyword);
        return "admin/school-manage";
    }

    @PostMapping("/schools/add")
    @ResponseBody
    public boolean addSchool(@RequestBody School school) {
        return schoolService.addSchool(school);
    }

    @PostMapping("/schools/update")
    @ResponseBody
    public boolean updateSchool(@RequestBody School school) {
        return schoolService.updateSchool(school);
    }

    @PostMapping("/schools/delete/{id}")
    @ResponseBody
    public boolean deleteSchool(@PathVariable Integer id) {
        return schoolService.deleteSchool(id);
    }

    @PostMapping("/schools/delete-batch")
    @ResponseBody
    public boolean deleteSchools(@RequestBody List<Integer> ids) {
        return schoolService.deleteSchools(ids);
    }
    // ============================================================
    //  6. 标签管理
    // ============================================================

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
        return tagService.addTag(tag);
    }

    @PostMapping("/tags/update")
    @ResponseBody
    public boolean updateTag(@RequestBody Tag tag) {
        return tagService.updateTag(tag);
    }

    @PostMapping("/tags/delete/{id}")
    @ResponseBody
    public boolean deleteTag(@PathVariable Integer id) {
        return tagService.deleteTag(id);
    }

    @PostMapping("/tags/delete-batch")
    @ResponseBody
    public boolean deleteTags(@RequestBody List<Integer> ids) {
        return tagService.deleteTags(ids);
    }

    // ============================================================
    //  7. 评论管理
    // ============================================================

    @GetMapping("/comment-manage")
    public String commentManage(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) Integer statusFilter,
                                Model model) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (statusFilter != null) {
            wrapper.eq(Comment::getStatus, statusFilter);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> commentPage = commentService.page(page, wrapper);
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("statusFilter", statusFilter);
        return "admin/comment-manage";
    }

    @PostMapping("/comment/status/{id}")
    @ResponseBody
    public boolean updateCommentStatus(@PathVariable Integer id, @RequestParam Integer status) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        comment.setUpdateTime(LocalDateTime.now());
        return commentService.updateById(comment);
    }

    @GetMapping("/comment/delete/{id}")
    @ResponseBody
    public boolean deleteComment(@PathVariable Integer id) {
        return commentService.removeById(id);
    }

    // ============================================================
    //  8. 评价管理
    // ============================================================

    @GetMapping("/review-manage")
    public String reviewManage(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) Integer ratingFilter,
                               Model model) {
        Page<OrderReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderReview> wrapper = new LambdaQueryWrapper<>();

        if (ratingFilter != null && ratingFilter > 0) {
            wrapper.eq(OrderReview::getRating, ratingFilter);
        }

        wrapper.orderByDesc(OrderReview::getCreatedAt);
        IPage<OrderReview> reviewPage = orderReviewMapper.selectPage(page, wrapper);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("ratingFilter", ratingFilter);
        return "admin/review-manage";
    }

    @GetMapping("/review/delete/{id}")
    @ResponseBody
    public boolean deleteReview(@PathVariable Long id) {
        return orderReviewMapper.deleteById(id) > 0;
    }

    @PostMapping("/review/reply")
    @ResponseBody
    public boolean replyReview(@RequestParam Long id, @RequestParam String reply) {
        OrderReview review = orderReviewMapper.selectById(id);
        if (review != null) {
            review.setReply(reply);
            review.setReplyTime(LocalDateTime.now());
            return orderReviewMapper.updateById(review) > 0;
        }
        return false;
    }

    // ============================================================
    //  9. 仪表盘
    // ============================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long userCount = userService.count();
        long articleCount = articleService.count();
        long orderCount = orderService.count();
        long commentCount = commentService.count();

        model.addAttribute("userCount", userCount);
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("commentCount", commentCount);
        return "admin/dashboard";
    }
}