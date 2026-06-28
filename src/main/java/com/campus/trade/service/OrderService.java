package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Order;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface OrderService extends IService<Order> {

    // 单商品下单
    Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark);

    // 多商品下单（支持不同卖家，每个商品独立成单）
    List<Order> createOrder(Integer buyerId, List<Integer> articleIds, List<Integer> quantities, String address);

    // ========== 查询 ==========
    List<Order> getBuyerOrders(Integer buyerId);

    Order getOrderByNo(String orderNo);

    List<Order> getSellerOrders(Integer sellerId);

    // ========== 订单操作 ==========
    boolean cancelOrder(Long orderId, String reason);

    boolean confirmReceipt(Long orderId);

    boolean deleteOrder(Long orderId, Integer userId);

    boolean shipOrder(Long orderId);

    boolean updateStatus(Long id, Integer status);

    boolean adminDeleteOrder(Long orderId);

    // ========== 支付 ==========
    // 统计待支付总额
    BigDecimal calculatePendingAmount(Integer buyerId);

    // 确认支付
    boolean payOrder(Long orderId, Integer buyerId);

    // ========== 统计和搜索 ==========
    Map<String, Object> getOrderStats(Integer userId, String role);

    List<Order> searchOrders(Integer userId, String role, String keyword, Integer status);

    boolean isValidStatusTransition(int fromStatus, int toStatus);


}