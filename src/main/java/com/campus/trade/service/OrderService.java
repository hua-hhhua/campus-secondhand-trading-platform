package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark);
    List<Order> getBuyerOrders(Integer buyerId);
    Order getOrderByNo(String orderNo);
    boolean cancelOrder(Long orderId, String reason);
    boolean confirmReceipt(Long orderId);
    boolean deleteOrder(Long orderId, Integer userId);
    boolean reviewOrder(OrderReview review);
    OrderReview getOrderReview(Long orderId);
    List<Order> getSellerOrders(Integer sellerId);
    boolean shipOrder(Long orderId);
    boolean replyReview(Long reviewId, String reply, Integer sellerId);
    boolean updateStatus(Long id, Integer status);
    boolean adminDeleteOrder(Long orderId);
    Map<String, Object> getOrderStats(Integer userId, String role);
    List<Order> searchOrders(Integer userId, String role, String keyword, Integer status);
    boolean isValidStatusTransition(int fromStatus, int toStatus);
}
