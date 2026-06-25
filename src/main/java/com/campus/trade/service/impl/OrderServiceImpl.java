package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.OrderReview;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.mapper.OrderMapper;
import com.campus.trade.mapper.OrderReviewMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderReviewMapper orderReviewMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional
    public Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark) {
        // 1. 查询商品信息
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("商品不存在");
        }
        if (article.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + article.getStock());
        }

        // 2. 查询买家和卖家信息
        User buyer = userMapper.selectById(buyerId);
        User seller = userMapper.selectById(article.getUserId());

        // 3. 计算总金额
        BigDecimal totalAmount = article.getPrice().multiply(new BigDecimal(quantity));

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(article.getUserId());
        order.setArticleId(articleId);
        order.setArticleTitle(article.getTitle());
        order.setArticleCover(article.getCoverImage());
        order.setPrice(article.getPrice());
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待付款
        order.setBuyerName(buyer != null ? buyer.getNickname() : null);
        order.setBuyerPhone(buyer != null ? buyer.getPhone() : null);
        order.setBuyerAddress(address);
        order.setSellerName(seller != null ? seller.getNickname() : null);
        order.setSellerPhone(seller != null ? seller.getPhone() : null);
        order.setRemark(remark);

        orderMapper.insert(order);

        // 5. 扣减库存（用乐观锁或直接扣减）
        article.setStock(article.getStock() - quantity);
        articleMapper.updateById(article);

        return order;
    }

    @Override
    public List<Order> getBuyerOrders(Integer buyerId) {
        return orderMapper.selectByBuyerId(buyerId);
    }

    @Override
    public List<Order> getSellerOrders(Integer sellerId) {
        return orderMapper.selectBySellerId(sellerId);
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("当前状态无法取消订单");
        }

        int result = orderMapper.cancelOrder(orderId, reason);
        if (result > 0) {
            // 恢复库存
            Article article = articleMapper.selectById(order.getArticleId());
            if (article != null) {
                article.setStock(article.getStock() + order.getQuantity());
                articleMapper.updateById(article);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean confirmReceipt(Long orderId) {
        int result = orderMapper.confirmReceipt(orderId);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean shipOrder(Long orderId) {
        int result = orderMapper.shipOrder(orderId);
        return result > 0;
    }

    @Override
    public Map<String, Object> getOrderStats(Integer userId, String role) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        }

        long total = count(wrapper);
        long pendingPayment = count(wrapper.clone().eq(Order::getStatus, 0));
        long pendingShip = count(wrapper.clone().eq(Order::getStatus, 1));
        long shipped = count(wrapper.clone().eq(Order::getStatus, 2));
        long received = count(wrapper.clone().eq(Order::getStatus, 3));
        long cancelled = count(wrapper.clone().eq(Order::getStatus, 4));
        long completed = count(wrapper.clone().eq(Order::getStatus, 5));

        stats.put("total", total);
        stats.put("pendingPayment", pendingPayment);
        stats.put("pendingShip", pendingShip);
        stats.put("shipped", shipped);
        stats.put("received", received);
        stats.put("cancelled", cancelled);
        stats.put("completed", completed);

        return stats;
    }

    @Override
    @Transactional
    public boolean reviewOrder(OrderReview review) {
        // 验证订单是否存在且已收货
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null || order.getStatus() != 3) {
            throw new RuntimeException("该订单不可评价");
        }

        review.setCreatedAt(LocalDateTime.now());
        return orderReviewMapper.insert(review) > 0;
    }

    @Override
    public OrderReview getOrderReview(Long orderId) {
        return orderReviewMapper.selectByOrderId(orderId);
    }

    @Override
    @Transactional
    public boolean replyReview(Long reviewId, String reply) {
        OrderReview review = orderReviewMapper.selectById(reviewId);
        if (review == null) {
            return false;
        }
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        return orderReviewMapper.updateById(review) > 0;
    }
}