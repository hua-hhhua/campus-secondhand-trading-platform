package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId}")
    List<Order> selectByBuyerId(@Param("buyerId") Integer buyerId);

    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId}")
    List<Order> selectBySellerId(@Param("sellerId") Integer sellerId);

    @Update("UPDATE orders SET status = 4, cancel_reason = #{reason}, updated_at = NOW() WHERE id = #{orderId} AND status IN (0, 1)")
    int cancelOrder(@Param("orderId") Long orderId, @Param("reason") String reason);

    @Update("UPDATE orders SET status = 2, updated_at = NOW() WHERE id = #{orderId} AND status = 1")
    int shipOrder(@Param("orderId") Long orderId);

    @Update("UPDATE orders SET status = 3, updated_at = NOW() WHERE id = #{orderId} AND status = 2")
    int confirmReceipt(@Param("orderId") Long orderId);
}