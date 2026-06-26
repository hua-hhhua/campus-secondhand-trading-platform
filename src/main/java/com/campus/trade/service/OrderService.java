package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    // 买家功能
    Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark);
    List<Order> getBuyerOrders(Integer buyerId);
    Order getOrderByNo(String orderNo);
    boolean cancelOrder(Long orderId, String reason);
    boolean confirmReceipt(Long orderId);
    boolean deleteOrder(Long orderId, Integer userId);
    boolean reviewOrder(OrderReview review);
    OrderReview getOrderReview(Long orderId);

    // 卖家功能
    List<Order> getSellerOrders(Integer sellerId);
    boolean shipOrder(Long orderId);
    boolean replyReview(Long reviewId, String reply, Integer sellerId);

    // 管理员功能
    boolean updateStatus(Long id, Integer status);
    boolean adminDeleteOrder(Long orderId);

    // 通用功能
    Map<String, Object> getOrderStats(Integer userId, String role);
    List<Order> searchOrders(Integer userId, String role, String keyword, Integer status);
}
