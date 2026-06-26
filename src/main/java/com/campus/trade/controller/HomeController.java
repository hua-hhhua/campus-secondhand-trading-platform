package com.campus.trade.controller;

import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.School;
import com.campus.trade.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommentService commentService;


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

            // 统计数据看板数据
            long articleCount = articleService.count();
            long userCount = userService.count();
            long orderCount = orderService.count();
            long commentCount = commentService.count();

            model.addAttribute("articleCount", articleCount);
            model.addAttribute("userCount", userCount);
            model.addAttribute("orderCount", orderCount);
            model.addAttribute("commentCount", commentCount);
            model.addAttribute("reviewCount", 0);

        } catch (Exception e) {
            System.out.println("首页加载商品列表失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("articles", new ArrayList<>());
            model.addAttribute("schools", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("articleCount", 0);
            model.addAttribute("userCount", 0);
            model.addAttribute("orderCount", 0);
            model.addAttribute("commentCount", 0);
            model.addAttribute("reviewCount", 0);
        }

        return "index";
    }

    /**
     * 处理 favicon.ico 请求，返回空响应避免报错
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.notFound().build();
    }

    /**
     * 个人中心页面
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/toLoginPage";
        }

        try {
            // 获取用户ID（这里从认证信息中获取用户名，然后查询用户）
            String username = auth.getName();
            
            // 这里需要通过用户名查询用户对象，暂时使用 userService
            // 如果 UserService 没有这个方法，可以使用其他方式
            com.campus.trade.entity.User user = getUserFromAuth(auth);
            
            if (user != null) {
                // 获取购物车商品数量
                int cartCount = shoppingCartService.getCartCount(user.getId());
                model.addAttribute("cartCount", cartCount);
                
                // 获取收藏数量（需要添加这个方法到 FavoriteService）
                // 暂时传 0，后续可以优化
                model.addAttribute("favoriteCount", 0);
            }
        } catch (Exception e) {
            System.out.println("个人中心加载失败: " + e.getMessage());
            e.printStackTrace();
        }

        return "personal-center";
    }

    /**
     * 从认证信息中获取用户对象
     */
    private com.campus.trade.entity.User getUserFromAuth(Authentication auth) {
        try {
            Object principal = auth.getPrincipal();
            com.campus.trade.entity.User user = null;
            
            if (principal instanceof com.campus.trade.entity.User) {
                user = (com.campus.trade.entity.User) principal;
            }
            
            return user;
        } catch (Exception e) {
            System.out.println("获取用户信息失败: " + e.getMessage());
            return null;
        }
    }
}
