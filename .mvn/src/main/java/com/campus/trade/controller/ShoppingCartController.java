package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.User;
import com.campus.trade.service.ShoppingCartService;
import com.campus.trade.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class ShoppingCartController {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    /**
     * 加入购物车
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> add(@RequestParam Integer articleId, 
                                    @RequestParam(defaultValue = "1") Integer quantity) {
        Map<String, Object> result = new HashMap<>();
        
        logger.info("========== 收到加入购物车请求 ==========");
        logger.info("articleId: {}, quantity: {}", articleId, quantity);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("用户未登录");
            result.put("success", false);
            result.put("message", "请先登录");
            result.put("needLogin", true);
            return result;
        }
        
        try {
            User user = getUserFromAuth(auth);
            
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            boolean success = shoppingCartService.addToCart(user.getId(), articleId, quantity);
            
            result.put("success", success);
            result.put("message", success ? "加入购物车成功" : "加入购物车失败");
            
            if (success) {
                int cartCount = shoppingCartService.getCartCount(user.getId());
                result.put("cartCount", cartCount);
            }
            
        } catch (Exception e) {
            logger.error("加入购物车异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 购物车页面
     */
    @GetMapping("")
    public String cartPage(Model model,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        logger.info("【购物车页面】进入页面，page: {}, size: {}", page, size);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            logger.warn("【购物车页面】用户未登录，跳转到登录页");
            return "redirect:/toLoginPage";
        }
        
        try {
            User user = getUserFromAuth(auth);
            
            if (user == null) {
                return "redirect:/toLoginPage";
            }
            
            IPage<ArticleVO> cartItems = shoppingCartService.getCartList(user.getId(), page, size);
            
            logger.info("【购物车页面】查询结果: total={}, records={}", 
                       cartItems.getTotal(), cartItems.getRecords().size());
            
            // 获取购物车元数据映射表（articleId -> cartInfo）
            java.util.Map<Integer, java.util.Map<String, Object>> cartInfoMap = 
                shoppingCartService.getCartInfoMap(user.getId());
            
            // 将购物车元数据（cartId、checked、quantity）填充到每个 ArticleVO 中
            for (ArticleVO item : cartItems.getRecords()) {
                java.util.Map<String, Object> info = cartInfoMap.get(item.getId());
                if (info != null) {
                    item.setCartId((Integer) info.get("cartId"));
                    item.setChecked((Integer) info.get("checked"));
                    item.setQuantity((Integer) info.get("quantity"));
                }
            }
            
            model.addAttribute("cartItems", cartItems.getRecords());
            model.addAttribute("cartPage", cartItems);
            
        } catch (Exception e) {
            logger.error("【购物车页面】异常：", e);
        }
        
        return "shopping-cart";
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/update-quantity")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Integer cartId,
                                               @RequestParam Integer quantity) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = shoppingCartService.updateQuantity(cartId, quantity);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            logger.error("更新数量异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除购物车商品
     */
    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> remove(@RequestParam Integer cartId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = shoppingCartService.removeFromCart(cartId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            logger.error("删除商品异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 清空购物车
     */
    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clear() {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        try {
            User user = getUserFromAuth(auth);
            boolean success = shoppingCartService.clearCart(user.getId());
            
            result.put("success", success);
            result.put("message", success ? "清空成功" : "清空失败");
        } catch (Exception e) {
            logger.error("清空购物车异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量删除
     */
    @PostMapping("/batch-remove")
    @ResponseBody
    public Map<String, Object> batchRemove(@RequestParam String cartIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = shoppingCartService.batchRemove(cartIds);
            result.put("success", success);
            result.put("message", success ? "批量删除成功" : "批量删除失败");
        } catch (Exception e) {
            logger.error("批量删除异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 切换选中状态
     */
    @PostMapping("/toggle-checked")
    @ResponseBody
    public Map<String, Object> toggleChecked(@RequestParam Integer cartId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = shoppingCartService.toggleChecked(cartId);
            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
        } catch (Exception e) {
            logger.error("切换选中状态异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 全选/取消全选
     */
    @PostMapping("/toggle-all-checked")
    @ResponseBody
    public Map<String, Object> toggleAllChecked(@RequestParam Integer checked) {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        try {
            User user = getUserFromAuth(auth);
            boolean success = shoppingCartService.toggleAllChecked(user.getId(), checked);
            
            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
        } catch (Exception e) {
            logger.error("全选操作异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 去结算页面（获取选中的商品）
     */
    @GetMapping("/checkout")
    public String checkout(Model model,
                           @RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/toLoginPage";
        }

        try {
            User user = getUserFromAuth(auth);
            IPage<ArticleVO> checkedItems = shoppingCartService.getCheckedItems(user.getId(), page, size);

            model.addAttribute("checkedItems", checkedItems.getRecords());
            model.addAttribute("checkedPage", checkedItems);

        } catch (Exception e) {
            logger.error("结算页面异常：", e);
        }

        return "checkout";  // ← 修改这里
    }

    /**
     * 从认证信息中获取用户对象
     */
    private User getUserFromAuth(Authentication auth) {
        try {
            Object principal = auth.getPrincipal();
            User user = null;
            
            if (principal instanceof User) {
                user = (User) principal;
            } else {
                String username = auth.getName();
                user = userService.getUserByUsername(username);
            }
            
            return user;
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return null;
        }
    }
}
