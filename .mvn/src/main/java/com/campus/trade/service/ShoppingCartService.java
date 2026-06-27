package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 加入购物车
     */
    boolean addToCart(Integer userId, Integer articleId, Integer quantity);

    /**
     * 查看购物车列表（分页）
     */
    IPage<ArticleVO> getCartList(Integer userId, Integer page, Integer size);

    /**
     * 更新商品数量
     */
    boolean updateQuantity(Integer cartId, Integer quantity);

    /**
     * 删除购物车商品
     */
    boolean removeFromCart(Integer cartId);

    /**
     * 清空购物车
     */
    boolean clearCart(Integer userId);

    /**
     * 批量删除购物车商品
     */
    boolean batchRemove(String cartIds);

    /**
     * 切换单个商品选中状态
     */
    boolean toggleChecked(Integer cartId);

    /**
     * 全选/取消全选
     */
    boolean toggleAllChecked(Integer userId, Integer checked);

    /**
     * 获取选中的商品（用于结算）
     */
    IPage<ArticleVO> getCheckedItems(Integer userId, Integer page, Integer size);

    /**
     * 获取购物车商品总数
     */
    int getCartCount(Integer userId);

    /**
     * 获取购物车元数据映射表（商品ID -> 购物车信息）
     */
    java.util.Map<Integer, java.util.Map<String, Object>> getCartInfoMap(Integer userId);
}
