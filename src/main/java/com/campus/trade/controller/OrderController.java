package com.campus.trade.controller;

import com.campus.trade.entity.Order;
import com.campus.trade.entity.User;
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

/**
 * 【订单模块-控制层】订单控制器
 * 提供订单相关的HTTP接口，包括创建订单、取消订单、确认收货、发货、查询订单、支付订单等功能
 */
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
     * 【订单模块-获取当前登录用户】
     * 从Spring Security上下文中获取当前登录的用户信息，支持User类型和Spring Security内置User类型的principal
     * 
     * @return 当前登录的用户对象，未登录则返回null
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
     * 【订单模块-创建订单】
     * 创建订单，支持多商品批量下单，每个商品生成一个独立订单
     * 
     * @param params 包含商品ID列表、数量列表、收货地址的参数Map
     *               - articleIds: 商品ID列表
     *               - quantities: 对应商品的数量列表
     *               - address: 收货地址，为空时默认"校园内交易"
     * @return 包含创建结果的响应实体，成功时返回订单ID列表和订单号列表
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
     * 【订单模块-取消订单】
     * 买家取消自己的订单，仅允许取消待支付和待发货状态的订单，取消后库存会自动归还
     * 
     * @param id     订单ID
     * @param reason 取消原因，可选参数
     * @return 包含取消结果的响应实体
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
     * 【订单模块-确认收货】
     * 买家确认收货，将订单状态从已发货更新为已完成，仅买家本人可操作
     * 
     * @param id 订单ID
     * @return 包含确认结果的响应实体
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
     * 【订单模块-卖家发货】
     * 卖家对订单进行发货操作，将订单状态从待发货更新为已发货，仅卖家本人可操作
     * 
     * @param id 订单ID
     * @return 包含发货结果的响应实体
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
     * 【订单模块-获取买家订单列表】
     * 获取当前登录用户作为买家的所有订单列表
     * 
     * @return 包含买家订单列表的响应实体
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
     * 【订单模块-获取卖家订单列表】
     * 获取当前登录用户作为卖家的所有订单列表
     * 
     * @return 包含卖家订单列表的响应实体
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
     * 【订单模块-获取订单详情】
     * 根据订单ID获取订单的详细信息
     * 
     * @param id 订单ID
     * @return 包含订单详情的响应实体，订单不存在时返回404
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
     * 【订单模块-支付订单】
     * 买家支付订单，将订单状态从待支付更新为待发货，仅订单买家本人可操作，且订单状态必须为待支付
     * 
     * @param id 订单ID
     * @return 包含支付结果的响应实体
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
                result.put("message", "订单状态不正确，当前状态：" + order.getStatus());
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