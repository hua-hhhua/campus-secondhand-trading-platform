package com.campus.trade.controller;

import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import com.campus.trade.entity.User;
import com.campus.trade.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpSession session;

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        // 先从 Session 获取（UserContextInterceptor 已存入）
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            return user;
        }
        
        // 如果 Session 中没有，尝试从 SecurityContext 获取用户名再查询
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String username = auth.getName();
            // 这里需要注入 UserService，但由于已经有 UserContextInterceptor，理论上不会走到这里
            return null;
        }
        return null;
    }

    /**
     * 1. 创建订单（立即购买）
     * POST /api/orders/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam Integer articleId,
            @RequestParam Integer quantity,
            @RequestParam String address,
            @RequestParam(required = false) String remark) {

        Map<String, Object> result = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }

            Order order = orderService.createOrder(user.getId(), articleId, quantity, address, remark);
            result.put("success", true);
            result.put("message", "订单创建成功");
            result.put("data", order);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 2. 获取我的订单（买家）
     * GET /api/orders/my
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyOrders() {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        List<Order> orders = orderService.getBuyerOrders(user.getId());
        result.put("success", true);
        result.put("data", orders);
        return ResponseEntity.ok(result);
    }

    /**
     * 3. 获取卖家的订单
     * GET /api/orders/seller
     */
    @GetMapping("/seller")
    public ResponseEntity<Map<String, Object>> getSellerOrders() {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        List<Order> orders = orderService.getSellerOrders(user.getId());
        result.put("success", true);
        result.put("data", orders);
        return ResponseEntity.ok(result);
    }

    /**
     * 4. 获取订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Order order = orderService.getById(id);
        if (order == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return ResponseEntity.notFound().build();
        }

        result.put("success", true);
        result.put("data", order);
        return ResponseEntity.ok(result);
    }

    /**
     * 5. 取消订单
     * POST /api/orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = orderService.cancelOrder(id, reason);
            result.put("success", success);
            result.put("message", success ? "订单已取消" : "取消失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 6. 确认收货
     * POST /api/orders/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReceipt(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = orderService.confirmReceipt(id);
        result.put("success", success);
        result.put("message", success ? "确认收货成功" : "操作失败");
        return ResponseEntity.ok(result);
    }

    /**
     * 7. 卖家发货
     * POST /api/orders/{id}/ship
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        // 验证卖家权限
        Order order = orderService.getById(id);
        System.out.println("=== 发货调试信息 ===");
        System.out.println("当前用户ID: " + user.getId());
        System.out.println("当前用户名: " + user.getUsername());
        System.out.println("订单ID: " + id);
        if (order != null) {
            System.out.println("订单seller_id: " + order.getSellerId());
        } else {
            System.out.println("订单不存在");
        }
        System.out.println("==================");
        
        if (order == null || !order.getSellerId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限操作");
            return ResponseEntity.status(403).body(result);
        }

        boolean success = orderService.shipOrder(id);
        result.put("success", success);
        result.put("message", success ? "发货成功" : "操作失败");
        return ResponseEntity.ok(result);
    }

    /**
     * 8. 评价订单
     * POST /api/orders/review
     */
    @PostMapping("/review")
    public ResponseEntity<Map<String, Object>> reviewOrder(@RequestBody OrderReview review) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }

            review.setBuyerId(user.getId());
            boolean success = orderService.reviewOrder(review);
            result.put("success", success);
            result.put("message", success ? "评价成功" : "评价失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 9. 获取订单评价
     * GET /api/orders/{id}/review
     */
    @GetMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> getOrderReview(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        OrderReview review = orderService.getOrderReview(id);
        result.put("success", true);
        result.put("data", review);
        return ResponseEntity.ok(result);
    }

    /**
     * 10. 订单状态统计
     * GET /api/orders/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Map<String, Object> stats = orderService.getOrderStats(user.getId(), "buyer");
        result.put("success", true);
        result.put("data", stats);
        return ResponseEntity.ok(result);
    }
}