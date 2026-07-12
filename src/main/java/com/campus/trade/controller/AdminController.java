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

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 【管理员后台模块-控制层】
 * 管理员后台控制器，提供订单管理、用户管理、商品管理、分类管理、
 * 学校管理、标签管理、评论管理和仪表盘统计等后台管理功能。
 * 
 * @author campus-trade
 */
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
    private AsyncService asyncService;

    @Autowired
    private EmailService emailService;

    // ============================================================
    // 1. 订单管理
    // ============================================================

    /**
     * 【管理员后台模块-订单管理功能】
     * 订单管理列表页面，支持分页查询、关键词搜索和状态筛选。
     * 关键词可匹配订单号、买家姓名、卖家姓名。
     *
     * @param pageNum      页码（默认1）
     * @param pageSize     每页数量（默认10）
     * @param keyword      搜索关键词（订单号/买家姓名/卖家姓名）
     * @param statusFilter 订单状态筛选
     * @param model        页面模型
     * @return 订单管理页面
     */
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
                        .orderByDesc("created_at"));
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        return "admin/order-manage";
    }

    /**
     * 【管理员后台模块-订单详情查询功能】
     * 根据订单ID获取订单详细信息，通过AJAX调用返回JSON数据。
     *
     * @param id 订单ID
     * @return 订单详情对象
     */
    @GetMapping("/orders/{id}")
    @ResponseBody
    public Order getOrderDetail(@PathVariable Long id) {
        return orderService.getById(id);
    }

    /**
     * 【管理员后台模块-订单状态更新功能】
     * 更新订单状态，通过路径参数传递订单ID，通过AJAX调用。
     *
     * @param id     订单ID
     * @param status 订单状态
     * @return 是否更新成功
     */
    @PostMapping("/orders/status/{id}")
    @ResponseBody
    public boolean updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }

    /**
     * 【管理员后台模块-订单状态更新功能（参数方式）】
     * 通过请求参数方式更新订单状态，通过AJAX调用。
     * 与updateOrderStatus功能相同，仅参数传递方式不同。
     *
     * @param id     订单ID
     * @param status 订单状态
     * @return 是否更新成功
     */
    @PostMapping("/orders/update-status")
    @ResponseBody
    public boolean updateOrderStatusByParam(@RequestParam Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }

    /**
     * 【管理员后台模块-订单删除功能】
     * 删除单个订单，通过AJAX调用。
     *
     * @param id 订单ID
     * @return 是否删除成功
     */
    @PostMapping("/orders/delete/{id}")
    @ResponseBody
    public boolean deleteOrder(@PathVariable Long id) {
        return orderService.adminDeleteOrder(id);
    }

    /**
     * 【管理员后台模块-订单批量删除功能】
     * 批量删除多个订单，通过AJAX调用。
     *
     * @param ids 订单ID列表
     * @return 是否删除成功
     */
    @PostMapping("/orders/delete-batch")
    @ResponseBody
    public boolean deleteOrders(@RequestBody List<Long> ids) {
        return orderService.removeByIds(ids);
    }

    // ============================================================
    // 2. 用户管理
    // ============================================================

    /**
     * 【管理员后台模块-用户管理功能】
     * 用户管理列表页面，支持分页查询、关键词搜索和注册时间范围筛选。
     * 关键词可匹配用户名、昵称、邮箱、手机号。
     *
     * @param pageNum   页码（默认1）
     * @param pageSize  每页数量（默认10）
     * @param keyword   搜索关键词（用户名/昵称/邮箱/手机号）
     * @param startTime 注册开始时间（格式：yyyy-MM-dd）
     * @param endTime   注册结束时间（格式：yyyy-MM-dd）
     * @param model     页面模型
     * @return 用户管理页面
     */
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
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
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

    /**
     * 【管理员后台模块-新增用户页面功能】
     * 跳转到新增用户表单页面。
     *
     * @param model 页面模型
     * @return 用户表单页面（新增模式）
     */
    @GetMapping("/users/add")
    public String addUserPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    /**
     * 【管理员后台模块-编辑用户页面功能】
     * 跳转到编辑用户表单页面，根据用户ID获取用户信息。
     * 如果用户不存在则重定向到用户列表页面。
     *
     * @param id    用户ID
     * @param model 页面模型
     * @return 用户表单页面（编辑模式）
     */
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

    /**
     * 【管理员后台模块-用户保存功能】
     * 保存用户信息，支持新增和编辑两种模式。
     * 新增时设置默认角色（普通用户）和状态（正常）；
     * 编辑时如果密码为空则保留原有密码。
     *
     * @param user 用户实体对象
     * @return 重定向到用户管理页面
     */
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

    /**
     * 【管理员后台模块-用户删除功能】
     * 删除单个用户。
     *
     * @param id 用户ID
     * @return 重定向到用户管理页面
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.removeById(id);
        return "redirect:/admin/users";
    }

    /**
     * 【管理员后台模块-用户批量删除功能】
     * 批量删除多个用户，用户ID以逗号分隔的字符串形式传入。
     *
     * @param ids 用户ID字符串，逗号分隔（如：1,2,3）
     * @return 重定向到用户管理页面
     */
    @GetMapping("/users/batchDelete")
    public String batchDeleteUsers(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            userService.removeById(Integer.parseInt(id));
        }
        return "redirect:/admin/users";
    }

    // ============================================================
    // 3. 商品管理（文章管理）- 合并版本，支持权限控制
    // ============================================================

    /**
     * 商品管理列表页面
     * 管理员可查看所有商品，普通用户只能查看自己发布的商品
     *
     * @param pageNum      页码（默认1）
     * @param pageSize     每页数量（默认10）
     * @param keyword      关键词搜索（标题或内容）
     * @param statusFilter 状态筛选（0=草稿，1=已发布，2=定时发布）
     * @param categoryId   分类ID筛选
     * @param startTime    开始时间筛选
     * @param endTime      结束时间筛选
     * @param session      用户会话
     * @param model        页面模型
     * @return 商品管理页面
     */
    @GetMapping("/articles")
    public String articleManage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer statusFilter,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpSession session,
            Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Page<Article> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        // 非管理员只能看到自己的商品
        if (currentUser != null && currentUser.getRole() != 1) {
            wrapper.eq("a.user_id", currentUser.getId());
        }

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

    /**
     * 商品发布/编辑表单页面（管理员入口）
     * 编辑时检查权限：只能编辑自己发布的商品或管理员可编辑所有商品
     *
     * @param id      商品ID（编辑时传入，新增时不传）
     * @param session 用户会话
     * @param model   页面模型
     * @return 商品表单页面
     */
    @GetMapping("/articles/form")
    public String articleForm(@RequestParam(required = false) Integer id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Article article;
        boolean isEdit = false;

        if (id != null) {
            article = articleService.getById(id);
            isEdit = true;
            // 检查权限：管理员或本人
            if (article != null && currentUser != null
                    && currentUser.getRole() != 1
                    && !article.getUserId().equals(currentUser.getId())) {
                return "redirect:/admin/articles";
            }
            List<Integer> tagIds = articleService.getTagIdsByArticleId(id);
            article.setTagIds(tagIds);
        } else {
            article = new Article();
        }

        model.addAttribute("article", article);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("isAdmin", true);
        model.addAttribute("categories", categoryService.list());
        model.addAttribute("schools", schoolService.list());
        model.addAttribute("tags", tagService.list());
        return "admin/article-form";
    }

    /**
     * 保存商品（管理员入口）
     * 处理商品新增和编辑，支持立即发布、保存草稿、定时发布三种状态
     * 编辑时保留原有发布时间，避免误更新
     *
     * @param article       商品实体
     * @param status        发布状态（0=草稿，1=立即发布，2=定时发布）
     * @param scheduledTime 定时发布时间（格式：yyyy-MM-dd'T'HH:mm）
     * @param sendEmail     是否发送邮件通知（1=发送，0=不发送）
     * @param tagIds        标签ID列表
     * @param session       用户会话
     * @return 重定向到商品管理页面
     */
    @PostMapping("/articles/save")
    public String saveArticle(Article article,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String scheduledTime,
            @RequestParam(required = false) Integer sendEmail,
            @RequestParam(required = false) List<Integer> tagIds,
            HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        System.out.println("========== 编辑商品 DEBUG ==========");
        System.out.println("商品ID: " + article.getId());
        System.out.println("接收到的标签IDs: " + tagIds);

        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        LocalDateTime now = LocalDateTime.now(shanghaiZone);
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        article.setUserId(currentUser.getId());

        if (article.getId() == null) {
            // 新增文章
            article.setViewCount(0);
            article.setIsTop(0);
            article.setAllowComment(1);
            article.setSendEmail(sendEmail != null ? sendEmail : 0);
            article.setCreateTime(now);
            article.setUpdateTime(now);

            if (status != null) {
                article.setStatus(status);
            } else if (article.getStatus() == null) {
                article.setStatus(1);
            }

            // 处理商品状态
            if (article.getStatus() == 0) {
                article.setProductStatus(2);
                article.setPublishedAt(null);
            } else if (article.getStatus() == 2) {
                article.setProductStatus(2);
                if (scheduledTime != null && !scheduledTime.isEmpty()) {
                    try {
                        LocalDateTime parsedTime = LocalDateTime.parse(scheduledTime, inputFormatter);
                        article.setPublishedAt(parsedTime.atZone(shanghaiZone).toLocalDateTime());
                    } catch (Exception e) {
                        article.setPublishedAt(now.plusHours(1));
                    }
                } else {
                    article.setPublishedAt(now.plusHours(1));
                }
            } else {
                article.setProductStatus(0);
                if (article.getPublishedAt() == null) {
                    article.setPublishedAt(now);
                }
            }

            articleService.save(article);

            if (tagIds != null && !tagIds.isEmpty()) {
                articleService.saveArticleTags(article.getId(), tagIds);
            }

            if (article.getStatus() == 1 && article.getSendEmail() != null && article.getSendEmail() == 1) {
                asyncService.asyncSendArticleNotification(article, "publish");
                System.out.println("【邮件通知】立即发布文章已触发邮件发送 - ID: " + article.getId());
            }
        } else {
            // 更新文章
            article.setUpdateTime(now);
            Article existing = articleService.getById(article.getId());
            if (existing != null) {
                // 权限检查
                if (currentUser.getRole() != 1 && !existing.getUserId().equals(currentUser.getId())) {
                    return "redirect:/admin/articles";
                }

                article.setUserId(existing.getUserId());

                if (status != null) {
                    article.setStatus(status);
                } else if (article.getStatus() == null) {
                    article.setStatus(existing.getStatus());
                }

                if (sendEmail != null) {
                    article.setSendEmail(sendEmail);
                } else if (article.getSendEmail() == null) {
                    article.setSendEmail(existing.getSendEmail());
                }

                if (article.getStatus() == 0) {
                    article.setProductStatus(2);
                    article.setPublishedAt(null);
                } else if (article.getStatus() == 2) {
                    article.setProductStatus(2);
                    if (scheduledTime != null && !scheduledTime.isEmpty()) {
                        try {
                            LocalDateTime parsedTime = LocalDateTime.parse(scheduledTime, inputFormatter);
                            article.setPublishedAt(parsedTime.atZone(shanghaiZone).toLocalDateTime());
                        } catch (Exception e) {
                            if (existing.getPublishedAt() != null) {
                                article.setPublishedAt(existing.getPublishedAt());
                            } else {
                                article.setPublishedAt(now.plusHours(1));
                            }
                        }
                    } else {
                        if (existing.getPublishedAt() != null) {
                            article.setPublishedAt(existing.getPublishedAt());
                        } else {
                            article.setPublishedAt(now.plusHours(1));
                        }
                    }
                } else {
                    if (existing.getProductStatus() == 2) {
                        article.setProductStatus(0);
                    } else {
                        article.setProductStatus(existing.getProductStatus());
                    }
                    if (article.getPublishedAt() == null) {
                        article.setPublishedAt(existing.getPublishedAt() != null ? existing.getPublishedAt() : now);
                    }
                }
            }
            articleService.updateById(article);

            if (tagIds == null) {
                tagIds = new ArrayList<>();
            }
            articleService.saveArticleTags(article.getId(), tagIds);
        }
        return "redirect:/admin/articles";
    }

    /**
     * 删除商品（管理员入口）
     * 管理员可删除任意商品，普通用户只能删除自己发布的商品
     * 删除后自动清除Redis缓存
     *
     * @param id      商品ID
     * @param session 用户会话
     * @return 重定向到商品管理页面
     */
    @GetMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Article article = articleService.getById(id);

        if (article != null && currentUser != null
                && (currentUser.getRole() == 1 || article.getUserId().equals(currentUser.getId()))) {
            articleService.removeById(id);
        }
        return "redirect:/admin/articles";
    }

    /**
     * 批量删除商品（管理员入口）
     * 批量删除多个商品，每个商品都检查权限
     *
     * @param ids     商品ID字符串，逗号分隔（如：1,2,3）
     * @param session 用户会话
     * @return 重定向到商品管理页面
     */
    @GetMapping("/articles/batchDelete")
    public String batchDeleteArticles(@RequestParam String ids, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            Article article = articleService.getById(Integer.parseInt(id));
            if (article != null && currentUser != null
                    && (currentUser.getRole() == 1 || article.getUserId().equals(currentUser.getId()))) {
                articleService.removeById(Integer.parseInt(id));
            }
        }
        return "redirect:/admin/articles";
    }

    /**
     * 更新商品发布状态（API接口）
     * 通过AJAX调用，仅更新商品的status字段（发布状态）
     *
     * @param id     商品ID
     * @param status 发布状态（0=草稿，1=已发布，2=定时发布）
     * @return 是否更新成功
     */
    @PostMapping("/articles/status/{id}")
    @ResponseBody
    public boolean updateArticleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(status);
        article.setUpdateTime(LocalDateTime.now());
        return articleService.updateById(article);
    }

    /**
     * 下架商品（管理员入口）
     * 将商品状态从在售改为已下架，下架后商品不再在首页展示
     * 管理员可下架任意商品，普通用户只能下架自己发布的商品
     *
     * @param id      商品ID
     * @param session 用户会话
     * @return 重定向到商品管理页面
     */
    @GetMapping("/articles/off-shelf/{id}")
    public String offShelf(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        Article article = articleService.getById(id);

        if (article != null && (currentUser.getRole() == 1 || article.getUserId().equals(currentUser.getId()))) {
            article.setProductStatus(2);
            article.setUpdateTime(LocalDateTime.now());
            articleService.updateById(article);
        }
        return "redirect:/admin/articles";
    }

    /**
     * 上架商品（管理员入口）
     * 将商品状态从已下架改为在售，上架后商品在首页展示
     * 管理员可上架任意商品，普通用户只能上架自己发布的商品
     *
     * @param id      商品ID
     * @param session 用户会话
     * @return 重定向到商品管理页面
     */
    @GetMapping("/articles/on-shelf/{id}")
    public String onShelf(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/toLoginPage";
        }

        Article article = articleService.getById(id);

        if (article != null && (currentUser.getRole() == 1 || article.getUserId().equals(currentUser.getId()))) {
            article.setProductStatus(0);
            article.setUpdateTime(LocalDateTime.now());
            articleService.updateById(article);
        }
        return "redirect:/admin/articles";
    }

    // ============================================================
    // 4. 分类管理
    // ============================================================

    /**
     * 【管理员后台模块-分类管理功能】
     * 分类管理列表页面，支持分页查询和关键词搜索。
     * 同时获取全部分类列表供页面使用。
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param keyword  搜索关键词
     * @param model    页面模型
     * @return 分类管理页面
     */
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

    /**
     * 【管理员后台模块-新增分类功能】
     * 新增分类，通过AJAX调用。
     *
     * @param category 分类实体对象
     * @return 是否新增成功
     */
    @PostMapping("/categories/add")
    @ResponseBody
    public boolean addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    /**
     * 【管理员后台模块-更新分类功能】
     * 更新分类信息，通过AJAX调用。
     *
     * @param category 分类实体对象
     * @return 是否更新成功
     */
    @PostMapping("/categories/update")
    @ResponseBody
    public boolean updateCategory(@RequestBody Category category) {
        return categoryService.updateCategory(category);
    }

    /**
     * 【管理员后台模块-删除分类功能】
     * 删除单个分类，通过AJAX调用。
     *
     * @param id 分类ID
     * @return 是否删除成功
     */
    @PostMapping("/categories/delete/{id}")
    @ResponseBody
    public boolean deleteCategory(@PathVariable Integer id) {
        return categoryService.deleteCategory(id);
    }

    /**
     * 【管理员后台模块-批量删除分类功能】
     * 批量删除多个分类，通过AJAX调用。
     *
     * @param ids 分类ID列表
     * @return 是否删除成功
     */
    @PostMapping("/categories/delete-batch")
    @ResponseBody
    public boolean deleteCategories(@RequestBody List<Integer> ids) {
        return categoryService.deleteCategories(ids);
    }

    // ============================================================
    // 5. 学校管理
    // ============================================================

    /**
     * 【管理员后台模块-学校管理功能】
     * 学校管理列表页面，支持分页查询和关键词搜索。
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param keyword  搜索关键词
     * @param model    页面模型
     * @return 学校管理页面
     */
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

    /**
     * 【管理员后台模块-新增学校功能】
     * 新增学校，通过AJAX调用。
     *
     * @param school 学校实体对象
     * @return 是否新增成功
     */
    @PostMapping("/schools/add")
    @ResponseBody
    public boolean addSchool(@RequestBody School school) {
        return schoolService.addSchool(school);
    }

    /**
     * 【管理员后台模块-更新学校功能】
     * 更新学校信息，通过AJAX调用。
     *
     * @param school 学校实体对象
     * @return 是否更新成功
     */
    @PostMapping("/schools/update")
    @ResponseBody
    public boolean updateSchool(@RequestBody School school) {
        return schoolService.updateSchool(school);
    }

    /**
     * 【管理员后台模块-删除学校功能】
     * 删除单个学校，通过AJAX调用。
     *
     * @param id 学校ID
     * @return 是否删除成功
     */
    @PostMapping("/schools/delete/{id}")
    @ResponseBody
    public boolean deleteSchool(@PathVariable Integer id) {
        return schoolService.deleteSchool(id);
    }

    /**
     * 【管理员后台模块-批量删除学校功能】
     * 批量删除多个学校，通过AJAX调用。
     *
     * @param ids 学校ID列表
     * @return 是否删除成功
     */
    @PostMapping("/schools/delete-batch")
    @ResponseBody
    public boolean deleteSchools(@RequestBody List<Integer> ids) {
        return schoolService.deleteSchools(ids);
    }

    // ============================================================
    // 6. 标签管理
    // ============================================================

    /**
     * 【管理员后台模块-标签管理功能】
     * 标签管理列表页面，支持分页查询和关键词搜索。
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param keyword  搜索关键词
     * @param model    页面模型
     * @return 标签管理页面
     */
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

    /**
     * 【管理员后台模块-新增标签功能】
     * 新增标签，通过AJAX调用。
     *
     * @param tag 标签实体对象
     * @return 是否新增成功
     */
    @PostMapping("/tags/add")
    @ResponseBody
    public boolean addTag(@RequestBody Tag tag) {
        return tagService.addTag(tag);
    }

    /**
     * 【管理员后台模块-更新标签功能】
     * 更新标签信息，通过AJAX调用。
     *
     * @param tag 标签实体对象
     * @return 是否更新成功
     */
    @PostMapping("/tags/update")
    @ResponseBody
    public boolean updateTag(@RequestBody Tag tag) {
        return tagService.updateTag(tag);
    }

    /**
     * 【管理员后台模块-删除标签功能】
     * 删除单个标签，通过AJAX调用。
     *
     * @param id 标签ID
     * @return 是否删除成功
     */
    @PostMapping("/tags/delete/{id}")
    @ResponseBody
    public boolean deleteTag(@PathVariable Integer id) {
        return tagService.deleteTag(id);
    }

    /**
     * 【管理员后台模块-批量删除标签功能】
     * 批量删除多个标签，通过AJAX调用。
     *
     * @param ids 标签ID列表
     * @return 是否删除成功
     */
    @PostMapping("/tags/delete-batch")
    @ResponseBody
    public boolean deleteTags(@RequestBody List<Integer> ids) {
        return tagService.deleteTags(ids);
    }

    // ============================================================
    // 7. 评论管理
    // ============================================================

    /**
     * 【管理员后台模块-评论管理功能】
     * 评论管理列表页面，支持分页查询和状态筛选。
     *
     * @param pageNum      页码（默认1）
     * @param pageSize     每页数量（默认10）
     * @param statusFilter 评论状态筛选
     * @param model        页面模型
     * @return 评论管理页面
     */
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

    /**
     * 【管理员后台模块-更新评论状态功能】
     * 更新评论状态（如审核通过、审核不通过等），通过AJAX调用。
     *
     * @param id     评论ID
     * @param status 评论状态
     * @return 是否更新成功
     */
    @PostMapping("/comment/status/{id}")
    @ResponseBody
    public boolean updateCommentStatus(@PathVariable Integer id, @RequestParam Integer status) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        comment.setUpdateTime(LocalDateTime.now());
        return commentService.updateById(comment);
    }

    /**
     * 【管理员后台模块-删除评论功能】
     * 删除单个评论，通过AJAX调用。
     *
     * @param id 评论ID
     * @return 是否删除成功
     */
    @GetMapping("/comment/delete/{id}")
    @ResponseBody
    public boolean deleteComment(@PathVariable Integer id) {
        return commentService.removeById(id);
    }

    // ============================================================
    // 8. 仪表盘
    // ============================================================

    /**
     * 【管理员后台模块-仪表盘统计功能】
     * 仪表盘首页，展示平台核心数据统计，包括用户总数、商品总数、
     * 订单总数和评论总数。
     *
     * @param model 页面模型
     * @return 仪表盘页面
     */
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

    /**
     * 【管理员后台模块-测试邮件功能】
     * 测试邮件发送功能，发送一封测试邮件到指定邮箱，
     * 用于验证邮件服务配置是否正确。
     *
     * @param model 页面模型
     * @return 仪表盘页面（携带测试结果消息）
     */
    @GetMapping("/test-email")
    public String testEmail(Model model) {
        try {
            System.out.println("【测试邮件】开始发送测试邮件...");
            emailService.sendArticleNotificationEmail(
                    "1538292542@qq.com",
                    "测试邮件标题",
                    "这是一封测试邮件，用于验证邮件发送功能是否正常工作。",
                    "测试用户");
            System.out.println("【测试邮件】发送成功！");
            model.addAttribute("message", "邮件发送成功！请检查收件箱。");
        } catch (Exception e) {
            System.out.println("【测试邮件】发送失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("message", "邮件发送失败: " + e.getMessage());
        }
        return "admin/dashboard";
    }

    /**
     * 【管理员后台模块-测试邮件API功能】
     * 测试邮件发送功能的API接口，通过AJAX调用，
     * 直接返回测试结果字符串，用于验证邮件服务配置。
     *
     * @return 测试结果消息
     */
    @GetMapping("/api/test-email")
    @ResponseBody
    public String testEmailApi() {
        try {
            System.out.println("【测试邮件API】开始发送测试邮件...");
            emailService.sendArticleNotificationEmail(
                    "1538292542@qq.com",
                    "测试邮件标题",
                    "这是一封测试邮件，用于验证邮件发送功能是否正常工作。",
                    "测试用户");
            System.out.println("【测试邮件API】发送成功！");
            return "邮件发送成功！请检查收件箱。";
        } catch (Exception e) {
            System.out.println("【测试邮件API】发送失败: " + e.getMessage());
            e.printStackTrace();
            return "邮件发送失败: " + e.getMessage();
        }
    }
}