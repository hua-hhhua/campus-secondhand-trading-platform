package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Order;
import com.campus.trade.mapper.OrderMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 【订单模块-服务接口层】订单服务接口
 * 定义订单相关的业务逻辑接口，包括订单创建、查询、状态变更、支付、统计搜索等功能
 */
public interface OrderService extends IService<Order> {

    /**
     * 【订单模块-获取基础Mapper】
     * 获取订单的基础Mapper对象，用于执行数据库操作
     * 
     * @return 订单Mapper实例
     */
    @Override
    OrderMapper getBaseMapper();

    /**
     * 【订单模块-单商品下单】
     * 创建单个商品的订单，扣减库存并生成订单记录
     * 
     * @param buyerId   买家用户ID
     * @param articleId 商品ID
     * @param quantity  购买数量
     * @param address   收货地址
     * @param remark    订单备注
     * @return 创建成功的订单对象
     */
    Order createOrder(Integer buyerId, Integer articleId, Integer quantity, String address, String remark);

    /**
     * 【订单模块-多商品下单】
     * 批量创建多个商品的订单，每个商品生成一个独立订单，统一扣减库存
     * 
     * @param buyerId    买家用户ID
     * @param articleIds 商品ID列表
     * @param quantities 对应商品的数量列表
     * @param address    收货地址
     * @return 创建成功的订单对象列表
     */
    List<Order> createOrder(Integer buyerId, List<Integer> articleIds, List<Integer> quantities, String address);

    /**
     * 【订单模块-获取买家订单列表】
     * 根据买家ID查询该用户的所有订单
     * 
     * @param buyerId 买家用户ID
     * @return 买家的订单列表
     */
    List<Order> getBuyerOrders(Integer buyerId);

    /**
     * 【订单模块-根据订单号查询订单】
     * 根据订单编号查询订单详情
     * 
     * @param orderNo 订单编号
     * @return 订单对象，不存在则返回null
     */
    Order getOrderByNo(String orderNo);

    /**
     * 【订单模块-获取卖家订单列表】
     * 根据卖家ID查询该用户的所有售出订单
     * 
     * @param sellerId 卖家用户ID
     * @return 卖家的订单列表
     */
    List<Order> getSellerOrders(Integer sellerId);

    /**
     * 【订单模块-取消订单】
     * 取消订单，归还商品库存，记录订单状态变更历史
     * 
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 取消成功返回true，失败返回false
     */
    boolean cancelOrder(Long orderId, String reason);

    /**
     * 【订单模块-确认收货】
     * 买家确认收货，将订单状态从已发货更新为已完成
     * 
     * @param orderId 订单ID
     * @return 确认成功返回true，失败返回false
     */
    boolean confirmReceipt(Long orderId);

    /**
     * 【订单模块-删除订单】
     * 买家删除自己的订单，仅已取消或已完成状态的订单可删除
     * 
     * @param orderId 订单ID
     * @param userId  操作用户ID（用于权限校验）
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteOrder(Long orderId, Integer userId);

    /**
     * 【订单模块-卖家发货】
     * 卖家对订单进行发货操作，将订单状态从待发货更新为已发货
     * 
     * @param orderId 订单ID
     * @return 发货成功返回true，失败返回false
     */
    boolean shipOrder(Long orderId);

    /**
     * 【订单模块-更新订单状态】
     * 更新订单状态，并记录状态变更历史
     * 
     * @param id     订单ID
     * @param status 目标状态值
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 【订单模块-管理员删除订单】
     * 管理员删除订单，同时归还商品库存
     * 
     * @param orderId 订单ID
     * @return 删除成功返回true，失败返回false
     */
    boolean adminDeleteOrder(Long orderId);

    /**
     * 【订单模块-计算待支付金额】
     * 计算指定买家所有待支付订单的总金额
     * 
     * @param buyerId 买家用户ID
     * @return 待支付总金额
     */
    BigDecimal calculatePendingAmount(Integer buyerId);

    /**
     * 【订单模块-支付订单】
     * 支付订单，将订单状态从待支付更新为待发货
     * 
     * @param orderId 订单ID
     * @param buyerId 买家用户ID（用于权限校验）
     * @return 支付成功返回true，失败返回false
     */
    boolean payOrder(Long orderId, Integer buyerId);

    /**
     * 【订单模块-获取订单统计数据】
     * 获取用户的订单统计信息，包括各状态订单数量
     * 
     * @param userId 用户ID
     * @param role   用户角色（buyer-买家，seller-卖家）
     * @return 包含各状态订单数量的统计Map
     */
    Map<String, Object> getOrderStats(Integer userId, String role);

    /**
     * 【订单模块-搜索订单】
     * 根据关键词和状态搜索订单，支持按订单号和商品标题模糊搜索
     * 
     * @param userId  用户ID
     * @param role    用户角色（buyer-买家，seller-卖家）
     * @param keyword 搜索关键词
     * @param status  订单状态筛选，为null则不筛选
     * @return 符合条件的订单列表
     */
    List<Order> searchOrders(Integer userId, String role, String keyword, Integer status);

    /**
     * 【订单模块-校验状态流转合法性】
     * 校验订单状态流转是否合法，确保订单状态只能按照预设流程变更
     * 
     * @param fromStatus 当前状态
     * @param toStatus   目标状态
     * @return 合法返回true，非法返回false
     */
    boolean isValidStatusTransition(int fromStatus, int toStatus);
}