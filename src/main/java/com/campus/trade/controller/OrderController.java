package com.campus.trade.controller;

import com.campus.trade.entity.User;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import com.campus.trade.service.OrderService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        if (principal instanceof org.springframework.security.core.userdetails.User) {
            String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            return userService.findByUsername(username);
        }

        return null;
    }

    /**
     * 创建订单（支持多商品下单，不同卖家分别成单）
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = getCurrentUser();
            if (user == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return ResponseEntity.status(401).body(result);
            }

            List<Integer> articleIds = (List<Integer>) params.get("articleIds");
            List<Integer> quantities = (List<Integer>) params.get("quantities");
            String address = (String) params.get("address");

            if (articleIds == null || articleIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择商品");
                return ResponseEntity.badRequest().body(result);
            }

            if (address == null || address.isEmpty()) {
                address = "校园内交易";
            }

            // 调用多商品下单（返回订单列表）
            List<Order> orders = orderService.createOrder(user.getId(), articleIds, quantities, address);

            List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<String> orderNos = orders.stream().map(Order::getOrderNo).collect(Collectors.toList());

            result.put("success", true);
            result.put("message", "订单创建成功，共 " + orders.size() + " 个订单");
            result.put("orderIds", orderIds);
            result.put("orderNos", orderNos);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 取消订单
     */
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

    /**
     * 确认收货
     */
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

    /**
     * 卖家发货
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取我的订单（买家）
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyOrders() {
        Map<String, Object> result = new HashMap<>();
        try {
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取卖家订单
     */
    @GetMapping("/seller")
    public ResponseEntity<Map<String, Object>> getSellerOrders() {
        Map<String, Object> result = new HashMap<>();
        try {
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return ResponseEntity.notFound().build();
            }
            result.put("success", true);
            result.put("data", order);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 评价订单
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
     * 获取订单评价
     */
    @GetMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> getOrderReview(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            OrderReview review = orderService.getOrderReview(id);
            result.put("success", true);
            result.put("data", review);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 支付订单（买家支付，状态从待付款改为待发货）
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
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

            if (!order.getBuyerId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "无权限支付此订单");
                return ResponseEntity.status(403).body(result);
            }

            if (order.getStatus() != 0) {
                result.put("success", false);
                result.put("message", "订单状态不正确，当前状态：" + order.getStatusText());
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = orderService.payOrder(id, user.getId());
            result.put("success", success);
            result.put("message", success ? "支付成功" : "支付失败");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}