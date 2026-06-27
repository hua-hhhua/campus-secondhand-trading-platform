package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.*;
import com.campus.trade.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }

    // ========== 单商品下单 ==========
    @Override
    @Transactional
    public Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("商品不存在");
        }
        if (article.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + article.getStock());
        }
        User buyer = userMapper.selectById(buyerId);
        User seller = userMapper.selectById(article.getUserId());
        BigDecimal totalAmount = article.getPrice().multiply(new BigDecimal(quantity));

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
        order.setStatus(0);
        order.setBuyerName(buyer != null ? buyer.getNickname() : null);
        order.setBuyerPhone(buyer != null ? buyer.getPhone() : null);
        order.setBuyerAddress(address);
        order.setSellerName(seller != null ? seller.getNickname() : null);
        order.setSellerPhone(seller != null ? seller.getPhone() : null);
        order.setRemark(remark);

        orderMapper.insert(order);

        article.setStock(article.getStock() - quantity);
        articleMapper.updateById(article);

        return order;
    }

    // ========== 多商品下单（支持不同卖家，每个商品独立成单） ==========
    @Override
    @Transactional
    public List<Order> createOrder(Integer buyerId, List<Integer> articleIds, List<Integer> quantities, String address) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new RuntimeException("请选择商品");
        }

        List<Order> orders = new ArrayList<>();
        User buyer = userMapper.selectById(buyerId);
        if (buyer == null) {
            throw new RuntimeException("买家不存在");
        }

        for (int i = 0; i < articleIds.size(); i++) {
            Integer articleId = articleIds.get(i);
            Integer quantity = (quantities != null && i < quantities.size()) ? quantities.get(i) : 1;

            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new RuntimeException("商品不存在: " + articleId);
            }
            if (article.getStock() < quantity) {
                throw new RuntimeException("商品 " + article.getTitle() + " 库存不足");
            }

            article.setStock(article.getStock() - quantity);
            articleMapper.updateById(article);

            BigDecimal totalAmount = article.getPrice().multiply(new BigDecimal(quantity));
            User seller = userMapper.selectById(article.getUserId());

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
            order.setStatus(0);
            order.setBuyerName(buyer.getNickname());
            order.setBuyerPhone(buyer.getPhone());
            order.setBuyerAddress(address);
            order.setSellerName(seller != null ? seller.getNickname() : null);
            order.setSellerPhone(seller != null ? seller.getPhone() : null);
            order.setRemark(null);

            orderMapper.insert(order);
            orders.add(order);
        }

        return orders;
    }

    // ========== 查询 ==========
    @Override
    public List<Order> getBuyerOrders(Integer buyerId) {
        return orderMapper.selectByBuyerId(buyerId);
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<Order> getSellerOrders(Integer sellerId) {
        return orderMapper.selectBySellerId(sellerId);
    }

    // ========== 订单操作 ==========
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
            orderStatusHistoryMapper.insertHistory(
                    orderId,
                    order.getStatus(),
                    4,
                    reason,
                    LocalDateTime.now()
            );
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
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2) {
            throw new RuntimeException("当前状态无法确认收货");
        }
        int result = orderMapper.confirmReceipt(orderId);
        if (result > 0) {
            orderStatusHistoryMapper.insertHistory(
                    orderId,
                    order.getStatus(),
                    3,
                    "买家确认收货",
                    LocalDateTime.now()
            );
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long orderId, Integer userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getBuyerId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != 4 && order.getStatus() != 5) {
            return false;
        }
        return orderMapper.deleteById(orderId) > 0;
    }

    @Override
    @Transactional
    public boolean shipOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            throw new RuntimeException("当前状态无法发货");
        }
        int result = orderMapper.shipOrder(orderId);
        if (result > 0) {
            orderStatusHistoryMapper.insertHistory(
                    orderId,
                    order.getStatus(),
                    2,
                    "卖家已发货",
                    LocalDateTime.now()
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Order order = getById(id);
        if (order == null) {
            return false;
        }
        Integer oldStatus = order.getStatus();
        order.setStatus(status);
        boolean result = updateById(order);
        if (result) {
            orderStatusHistoryMapper.insertHistory(
                    id,
                    oldStatus,
                    status,
                    "管理员修改状态",
                    LocalDateTime.now()
            );
        }
        return result;
    }

    @Override
    @Transactional
    public boolean adminDeleteOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        OrderReview review = orderReviewMapper.selectByOrderId(orderId);
        if (review != null) {
            orderReviewMapper.deleteById(review.getId());
        }
        Article article = articleMapper.selectById(order.getArticleId());
        if (article != null) {
            article.setStock(article.getStock() + order.getQuantity());
            articleMapper.updateById(article);
        }
        return orderMapper.deleteById(orderId) > 0;
    }

    // ========== 评价 ==========
    @Override
    @Transactional
    public boolean reviewOrder(OrderReview review) {
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null || order.getStatus() != 3) {
            throw new RuntimeException("该订单不可评价");
        }
        if (!order.getBuyerId().equals(review.getBuyerId())) {
            throw new RuntimeException("无权评价此订单");
        }
        OrderReview existingReview = orderReviewMapper.selectByOrderId(review.getOrderId());
        if (existingReview != null) {
            throw new RuntimeException("该订单已评价，不可重复评价");
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
    public boolean replyReview(Long reviewId, String reply, Integer sellerId) {
        OrderReview review = orderReviewMapper.selectById(reviewId);
        if (review == null) {
            return false;
        }
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null || !order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("无权回复此评价");
        }
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        return orderReviewMapper.updateById(review) > 0;
    }

    // ========== 统计和搜索 ==========
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
    public List<Order> searchOrders(Integer userId, String role, String keyword, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or()
                    .like(Order::getArticleTitle, keyword));
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        return list(wrapper);
    }

    @Override
    public boolean isValidStatusTransition(int currentStatus, int newStatus) {
        switch (currentStatus) {
            case 0:
                return newStatus == 1 || newStatus == 4;
            case 1:
                return newStatus == 2 || newStatus == 4;
            case 2:
                return newStatus == 3;
            case 3:
            case 4:
            case 5:
                return false;
            default:
                return false;
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkTimeoutOrders() {
        List<Order> timeoutOrders = lambdaQuery()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreatedAt, LocalDateTime.now().minusMinutes(30))
                .list();
        timeoutOrders.forEach(order -> {
            cancelOrder(order.getId(), "订单超时自动取消");
        });
    }

    @Async
    public void sendOrderNotification(Order order, String type) {
        // 实现发送订单通知逻辑
    }
}