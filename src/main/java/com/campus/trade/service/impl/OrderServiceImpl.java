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
import com.campus.trade.mapper.OrderStatusHistoryMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 买家功能实现
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

    @Override
    public List<Order> getBuyerOrders(Integer buyerId) {
        return orderMapper.selectByBuyerId(buyerId);
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
            // 记录状态变更历史
            orderStatusHistoryMapper.insertHistory(
                    orderId,
                    order.getStatus(),
                    3, // 已完成
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

    // 卖家功能实现
    @Override
    public List<Order> getSellerOrders(Integer sellerId) {
        return orderMapper.selectBySellerId(sellerId);
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
            // 记录状态变更历史
            orderStatusHistoryMapper.insertHistory(
                    orderId,
                    order.getStatus(),
                    2, // 已发货
                    "卖家已发货",
                    LocalDateTime.now()
            );
            return true;
        }
        return false;
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

    // 管理员功能实现
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
            // 记录状态变更历史
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

        // 删除相关评价
        OrderReview review = orderReviewMapper.selectByOrderId(orderId);
        if (review != null) {
            orderReviewMapper.deleteById(review.getId());
        }

        // 恢复库存
        Article article = articleMapper.selectById(order.getArticleId());
        if (article != null) {
            article.setStock(article.getStock() + order.getQuantity());
            articleMapper.updateById(article);
        }

        return orderMapper.deleteById(orderId) > 0;
    }

    // 通用功能实现
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

        return list(wrapper);  // 使用 list(wrapper) 而不是 lambdaUpdate().list()
    }


    // 定时任务
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


    // 异步通知
    @Async
    public void sendOrderNotification(Order order, String type) {
        // 实现发送邮件或站内信通知的逻辑
    }
}
