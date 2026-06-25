package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据买家ID查询订单列表（按创建时间倒序）
     */
    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} ORDER BY created_at DESC")
    List<Order> selectByBuyerId(@Param("buyerId") Integer buyerId);

    /**
     * 根据卖家ID查询订单列表（按创建时间倒序）
     */
    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Order> selectBySellerId(@Param("sellerId") Integer sellerId);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单状态
     */
    @Update("UPDATE orders SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 取消订单（带取消原因）
     */
    @Update("UPDATE orders SET status = 4, cancel_reason = #{reason}, updated_at = NOW() WHERE id = #{id} AND status IN (0, 1)")
    int cancelOrder(@Param("id") Long id, @Param("reason") String reason);

    /**
     * 确认收货
     */
    @Update("UPDATE orders SET status = 3, updated_at = NOW() WHERE id = #{id} AND status = 2")
    int confirmReceipt(@Param("id") Long id);

    /**
     * 卖家发货
     */
    @Update("UPDATE orders SET status = 2, updated_at = NOW() WHERE id = #{id} AND status = 1")
    int shipOrder(@Param("id") Long id);

    /**
     * 根据状态查询订单（分页用，配合MyBatis-Plus的Page）
     */
    @Select("SELECT * FROM orders WHERE status = #{status} ORDER BY created_at DESC")
    List<Order> selectByStatus(@Param("status") Integer status);
}