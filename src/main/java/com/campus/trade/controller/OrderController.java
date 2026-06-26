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

    private User getCurrentUser() {
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            return user;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String username = auth.getName();
            return null;
        }
        return null;
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Order order = orderService.getById(id);
        if (order == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return ResponseEntity.notFound().build();
        }

        boolean isBuyer = order.getBuyerId().equals(user.getId());
        boolean isSeller = order.getSellerId().equals(user.getId());
        boolean isAdmin = user.getRole() != null && "ADMIN".equals(user.getRole());

        if (!isBuyer && !isSeller && !isAdmin) {
            result.put("success", false);
            result.put("message", "无权限查看该订单");
            return ResponseEntity.status(403).body(result);
        }

        result.put("success", true);
        result.put("data", order);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        Map<String, Object> result = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }

            Order order = orderService.getById(id);
            if (order == null || !order.getBuyerId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "无权限操作或订单不存在");
                return ResponseEntity.status(403).body(result);
            }

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

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReceipt(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getBuyerId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限操作");
            return ResponseEntity.status(403).body(result);
        }

        boolean success = orderService.confirmReceipt(id);
        result.put("success", success);
        result.put("message", success ? "确认收货成功" : "操作失败");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        boolean success = orderService.deleteOrder(id, user.getId());
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return ResponseEntity.ok(result);
    }

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

    @GetMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> getOrderReview(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        OrderReview review = orderService.getOrderReview(id);
        result.put("success", true);
        result.put("data", review);
        return ResponseEntity.ok(result);
    }
}
