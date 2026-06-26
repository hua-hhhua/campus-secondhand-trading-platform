package com.campus.trade.controller;

import com.campus.trade.entity.User;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import com.campus.trade.entity.Result;
import com.campus.trade.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
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
        // 实现获取当前用户逻辑
        return null;
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
                result.put("message", "无权限操作");
                return ResponseEntity.status(403).body(result);
            }

            boolean success = orderService.confirmReceipt(id);
            result.put("success", success);
            result.put("message", success ? "确认收货成功" : "操作失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    // 其他方法保持不变...
}
