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

/**
 * 购物车模块-控制层
 * 
 * 负责处理购物车相关的HTTP请求，包括商品加入购物车、购物车列表展示、
 * 商品数量更新、商品删除、清空购物车、批量删除、选中状态切换、
 * 全选/取消全选以及结算页面跳转等功能。
 */
@Controller
@RequestMapping("/cart")
public class ShoppingCartController {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    /**
     * 【购物车模块-加入购物车功能】
     * 
     * 将指定商品添加到当前登录用户的购物车中。
     * 若用户未登录，则返回需要登录的提示；
     * 若商品已在购物车中，则增加商品数量；
     * 成功加入后返回购物车商品总数。
     * 
     * @param articleId 商品ID，标识要加入购物车的商品
     * @param quantity  商品数量，默认为1，表示加入购物车的商品件数
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
     *         - needLogin: 是否需要登录（仅未登录时返回）
     *         - cartCount: 购物车商品总数（仅成功时返回）
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

        } catch (RuntimeException e) {
            logger.error("加入购物车失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            logger.error("加入购物车异常：", e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 【购物车模块-购物车页面展示功能】
     * 
     * 展示当前登录用户的购物车页面，分页显示购物车中的商品列表。
     * 若用户未登录，则重定向到登录页面；
     * 同时将购物车元数据（cartId、选中状态、数量）填充到每个商品VO中。
     * 
     * @param model Spring MVC的Model对象，用于向视图传递数据
     * @param page  当前页码，默认为1，表示分页查询的当前页
     * @param size  每页显示数量，默认为10，表示每页显示的商品记录数
     * @return String 视图名称，返回shopping-cart购物车页面；
     *         若用户未登录则重定向到登录页面
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
            java.util.Map<Integer, java.util.Map<String, Object>> cartInfoMap = shoppingCartService
                    .getCartInfoMap(user.getId());

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
     * 【购物车模块-更新商品数量功能】
     * 
     * 更新购物车中指定商品的购买数量。
     * 根据购物车记录ID找到对应商品，将其数量更新为指定值。
     * 
     * @param cartId   购物车记录ID，标识要更新数量的购物车条目
     * @param quantity 新的商品数量，表示更新后的购买件数
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
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
     * 【购物车模块-删除购物车商品功能】
     * 
     * 从购物车中删除指定的商品记录。
     * 根据购物车记录ID删除对应的购物车条目。
     * 
     * @param cartId 购物车记录ID，标识要删除的购物车条目
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
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
     * 【购物车模块-清空购物车功能】
     * 
     * 清空当前登录用户购物车中的所有商品。
     * 若用户未登录，则返回需要登录的提示。
     * 
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
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
     * 【购物车模块-批量删除购物车商品功能】
     * 
     * 批量删除购物车中指定的多条商品记录。
     * 接收以逗号分隔的购物车记录ID字符串，解析后批量删除。
     * 
     * @param cartIds 购物车记录ID字符串，多个ID之间用逗号分隔，
     *                例如："1,2,3"
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
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
     * 【购物车模块-切换单个商品选中状态功能】
     * 
     * 切换购物车中单个商品的选中状态（选中/取消选中）。
     * 选中状态用于标识商品是否参与结算。
     * 
     * @param cartId 购物车记录ID，标识要切换选中状态的购物车条目
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
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
     * 【购物车模块-全选/取消全选功能】
     * 
     * 将当前登录用户购物车中的所有商品设置为选中或取消选中状态。
     * 若用户未登录，则返回需要登录的提示。
     * 
     * @param checked 选中状态，1表示全选，0表示取消全选
     * @return Map<String, Object> 操作结果，包含：
     *         - success: 操作是否成功
     *         - message: 操作结果提示信息
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
     * 【购物车模块-结算页面展示功能】
     * 
     * 跳转到结算页面，展示当前登录用户购物车中已选中的商品列表。
     * 若用户未登录，则重定向到登录页面。
     * 只显示选中的商品用于后续订单生成和支付操作。
     * 
     * @param model Spring MVC的Model对象，用于向视图传递数据
     * @param page  当前页码，默认为1，表示分页查询的当前页
     * @param size  每页显示数量，默认为10，表示每页显示的商品记录数
     * @return String 视图名称，返回checkout结算页面；
     *         若用户未登录则重定向到登录页面
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

        return "checkout"; // ← 修改这里
    }

    /**
     * 【购物车模块-从认证信息获取用户功能】
     * 
     * 从Spring Security的认证信息中获取当前登录用户对象。
     * 优先从Principal中直接获取User对象，若不是User类型则通过用户名查询用户信息。
     * 该方法为私有工具方法，供Controller内部其他方法调用。
     * 
     * @param auth Spring Security的认证对象，包含当前用户的认证信息
     * @return User 登录用户对象，若获取失败则返回null
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
