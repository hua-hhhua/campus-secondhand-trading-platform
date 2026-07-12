package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.mapper.OrderMapper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【订单模块-服务实现层】订单服务实现类
 * 实现订单相关的业务逻辑，包括订单创建、查询、状态变更、支付、统计搜索、超时自动取消等功能
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;

    /**
     * 【订单模块-生成订单编号】
     * 生成唯一的订单编号，格式为"ORD" + 当前时间戳
     * 
     * @return 订单编号字符串
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }

    /**
     * 【订单模块-单商品下单】
     * 创建单个商品的订单，校验商品存在性、库存、不能购买自己的商品，扣减库存并生成订单记录
     * 
     * @param buyerId   买家用户ID
     * @param articleId 商品ID
     * @param quantity  购买数量
     * @param address   收货地址
     * @param remark    订单备注
     * @return 创建成功的订单对象
     * @throws RuntimeException 商品不存在、库存不足、不能购买自己的商品等异常
     */
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
        if (article.getUserId().equals(buyerId)) {
            throw new RuntimeException("不能购买自己发布的商品");
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

    /**
     * 【订单模块-多商品下单】
     * 批量创建多个商品的订单，每个商品生成一个独立订单，统一扣减库存，支持批量校验
     * 
     * @param buyerId    买家用户ID
     * @param articleIds 商品ID列表
     * @param quantities 对应商品的数量列表，数量不足时默认1
     * @param address    收货地址
     * @return 创建成功的订单对象列表
     * @throws RuntimeException 商品不存在、库存不足、不能购买自己的商品等异常
     */
    @Override
    @Transactional
    public List<Order> createOrder(Integer buyerId, List<Integer> articleIds, List<Integer> quantities,
            String address) {
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
            if (article.getUserId().equals(buyerId)) {
                throw new RuntimeException("不能购买自己发布的商品: " + article.getTitle());
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

    /**
     * 【订单模块-获取买家订单列表】
     * 根据买家ID查询该用户的所有订单，按创建时间倒序排列
     * 
     * @param buyerId 买家用户ID
     * @return 买家的订单列表
     */
    @Override
    public List<Order> getBuyerOrders(Integer buyerId) {
        return orderMapper.selectByBuyerId(buyerId);
    }

    /**
     * 【订单模块-根据订单号查询订单】
     * 根据订单编号查询订单详情
     * 
     * @param orderNo 订单编号
     * @return 订单对象，不存在则返回null
     */
    @Override
    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 【订单模块-获取卖家订单列表】
     * 根据卖家ID查询该用户的所有售出订单，按创建时间倒序排列
     * 
     * @param sellerId 卖家用户ID
     * @return 卖家的订单列表
     */
    @Override
    public List<Order> getSellerOrders(Integer sellerId) {
        return orderMapper.selectBySellerId(sellerId);
    }

    /**
     * 【订单模块-取消订单】
     * 取消订单，仅允许取消待支付(0)和待发货(1)状态的订单，取消后归还商品库存并记录状态变更历史
     * 
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 取消成功返回true，失败返回false
     * @throws RuntimeException 当前状态无法取消订单时抛出
     */
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
                    LocalDateTime.now());
            Article article = articleMapper.selectById(order.getArticleId());
            if (article != null) {
                article.setStock(article.getStock() + order.getQuantity());
                articleMapper.updateById(article);
            }
            return true;
        }
        return false;
    }

    /**
     * 【订单模块-确认收货】
     * 买家确认收货，将订单状态从已发货(2)更新为已完成(3)，记录状态变更历史
     * 
     * @param orderId 订单ID
     * @return 确认成功返回true，失败返回false
     * @throws RuntimeException 当前状态无法确认收货时抛出
     */
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
                    LocalDateTime.now());
            return true;
        }
        return false;
    }

    /**
     * 【订单模块-删除订单】
     * 买家删除自己的订单，仅已取消(4)或已完成(5)状态的订单可删除
     * 
     * @param orderId 订单ID
     * @param userId  操作用户ID（用于权限校验）
     * @return 删除成功返回true，失败返回false
     */
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

    /**
     * 【订单模块-卖家发货】
     * 卖家对订单进行发货操作，将订单状态从待发货(1)更新为已发货(2)，记录状态变更历史
     * 
     * @param orderId 订单ID
     * @return 发货成功返回true，失败返回false
     * @throws RuntimeException 当前状态无法发货时抛出
     */
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
                    LocalDateTime.now());
            return true;
        }
        return false;
    }

    /**
     * 【订单模块-更新订单状态】
     * 更新订单状态，并记录状态变更历史，由管理员修改状态时调用
     * 
     * @param id     订单ID
     * @param status 目标状态值
     * @return 更新成功返回true，失败返回false
     */
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
                    LocalDateTime.now());
        }
        return result;
    }

    /**
     * 【订单模块-管理员删除订单】
     * 管理员删除订单，同时归还商品库存
     * 
     * @param orderId 订单ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean adminDeleteOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        Article article = articleMapper.selectById(order.getArticleId());
        if (article != null) {
            article.setStock(article.getStock() + order.getQuantity());
            articleMapper.updateById(article);
        }
        return orderMapper.deleteById(orderId) > 0;
    }

    /**
     * 【订单模块-计算待支付金额】
     * 计算指定买家所有待支付(状态0)订单的总金额
     * 
     * @param buyerId 买家用户ID
     * @return 待支付总金额，无待支付订单时返回0
     */
    @Override
    public BigDecimal calculatePendingAmount(Integer buyerId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, buyerId)
                .eq(Order::getStatus, 0);
        List<Order> orders = list(wrapper);
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 【订单模块-支付订单】
     * 支付订单，将订单状态从待支付(0)更新为待发货(1)，校验订单归属和状态
     * 
     * @param orderId 订单ID
     * @param buyerId 买家用户ID（用于权限校验）
     * @return 支付成功返回true，失败返回false
     * @throws RuntimeException 订单不存在、无权操作、状态不正确时抛出
     */
    @Override
    @Transactional
    public boolean payOrder(Long orderId, Integer buyerId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前状态无法支付，状态：" + order.getStatus());
        }
        return updateStatus(orderId, 1);
    }

    /**
     * 【订单模块-获取订单统计数据】
     * 获取用户的订单统计信息，包括总订单数及各状态（待支付、待发货、已发货、已完成、已取消、交易完成）的订单数量
     * 
     * @param userId 用户ID
     * @param role   用户角色（buyer-买家，seller-卖家）
     * @return 包含各状态订单数量的统计Map
     */
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

    /**
     * 【订单模块-搜索订单】
     * 根据关键词和状态搜索订单，支持按订单号和商品标题模糊搜索，可按用户角色筛选
     * 
     * @param userId  用户ID
     * @param role    用户角色（buyer-买家，seller-卖家）
     * @param keyword 搜索关键词，匹配订单号或商品标题
     * @param status  订单状态筛选，为null则不筛选
     * @return 符合条件的订单列表
     */
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

    /**
     * 【订单模块-校验状态流转合法性】
     * 校验订单状态流转是否合法，确保订单状态只能按照预设流程变更：
     * 0(待支付) → 1(待发货) 或 4(已取消)
     * 1(待发货) → 2(已发货) 或 4(已取消)
     * 2(已发货) → 3(已完成)
     * 3(已完成)、4(已取消)、5(交易完成) 为终态，不可变更
     * 
     * @param currentStatus 当前状态
     * @param newStatus     目标状态
     * @return 合法返回true，非法返回false
     */
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

    /**
     * 【订单模块-检查超时订单】
     * 定时任务，每分钟执行一次，自动取消创建超过30分钟仍未支付的订单并归还库存
     */
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

    /**
     * 【订单模块-发送订单通知】
     * 异步发送订单相关通知，如订单创建、支付、发货、收货等状态变更通知
     * 
     * @param order 订单对象
     * @param type  通知类型
     */
    @Async
    public void sendOrderNotification(Order order, String type) {
        // 实现发送订单通知逻辑
    }
}