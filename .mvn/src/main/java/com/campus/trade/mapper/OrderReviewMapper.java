package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {

    /**
     * 根据订单ID查询评价
     */
    @Select("SELECT * FROM order_reviews WHERE order_id = #{orderId}")
    OrderReview selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据商品ID查询评价列表
     */
    @Select("SELECT * FROM order_reviews WHERE article_id = #{articleId} ORDER BY created_at DESC")
    List<OrderReview> selectByArticleId(@Param("articleId") Integer articleId);

    /**
     * 根据买家ID查询评价列表
     */
    @Select("SELECT * FROM order_reviews WHERE buyer_id = #{buyerId} ORDER BY created_at DESC")
    List<OrderReview> selectByBuyerId(@Param("buyerId") Integer buyerId);

    /**
     * 根据卖家ID查询评价列表
     */
    @Select("SELECT * FROM order_reviews WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<OrderReview> selectBySellerId(@Param("sellerId") Integer sellerId);
}