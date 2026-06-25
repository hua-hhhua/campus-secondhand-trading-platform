package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    /**
     * 统计用户购物车中商品数量
     */
    @Select("SELECT COUNT(*) FROM shopping_cart WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);

    /**
     * 统计用户购物车中已选中的商品数量
     */
    @Select("SELECT COUNT(*) FROM shopping_cart WHERE user_id = #{userId} AND checked = 1")
    int countCheckedByUserId(@Param("userId") Integer userId);
}
