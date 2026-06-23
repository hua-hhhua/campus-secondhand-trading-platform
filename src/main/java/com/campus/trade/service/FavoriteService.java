package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.Favorite;

public interface FavoriteService extends IService<Favorite> {

    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    boolean toggleFavorite(Integer userId, Integer articleId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Integer userId, Integer articleId);

    /**
     * 获取收藏数量
     */
    int getFavoriteCount(Integer articleId);

    /**
     * 获取用户收藏的商品列表
     */
    IPage<ArticleVO> getUserFavorites(Integer userId, Integer page, Integer size);
}