package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 创建订单（立即购买）
     */
    Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark);

    /**
     * 获取买家订单列表
     */
    List<Order> getBuyerOrders(Integer buyerId);

    /**
     * 获取卖家订单列表
     */
    List<Order> getSellerOrders(Integer sellerId);

    /**
     * 根据订单号获取订单详情
     */
    Order getOrderByNo(String orderNo);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, String reason);

    /**
     * 确认收货
     */
    boolean confirmReceipt(Long orderId);

    /**
     * 卖家发货
     */
    boolean shipOrder(Long orderId);

    /**
     * 获取订单状态统计
     */
    Map<String, Object> getOrderStats(Integer userId, String role);

    /**
     * 评价订单
     */
    boolean reviewOrder(OrderReview review);

    /**
     * 获取订单评价
     */
    OrderReview getOrderReview(Long orderId);

    /**
     * 卖家回复评价
     */
    boolean replyReview(Long reviewId, String reply);
}