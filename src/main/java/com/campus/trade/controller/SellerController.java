package com.campus.trade.controller;

import com.campus.trade.entity.Order;
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
@RequestMapping("/api/seller/orders")
public class SellerController {

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

    @GetMapping
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

        if (!order.getSellerId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "无权限查看该订单");
            return ResponseEntity.status(403).body(result);
        }

        result.put("success", true);
        result.put("data", order);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {

        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser();
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        Order order = orderService.getById(id);
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

}
