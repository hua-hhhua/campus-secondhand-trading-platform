package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} ORDER BY created_at DESC")
    List<Order> selectByBuyerId(@Param("buyerId") Integer buyerId);

    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Order> selectBySellerId(@Param("sellerId") Integer sellerId);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("UPDATE orders SET status = 4, reason = #{reason}, updated_at = NOW() WHERE id = #{orderId}")
    int cancelOrder(@Param("orderId") Long orderId, @Param("reason") String reason);

    @Select("UPDATE orders SET status = 3, updated_at = NOW() WHERE id = #{orderId}")
    int confirmReceipt(@Param("orderId") Long orderId);

    @Select("UPDATE orders SET status = 2, tracking_number = #{trackingNumber}, updated_at = NOW() WHERE id = #{orderId}")
    int shipOrder(@Param("orderId") Long orderId);
}
