package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.Favorite;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.mapper.FavoriteMapper;
import com.campus.trade.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    @Transactional
    public boolean toggleFavorite(Integer userId, Integer articleId) {
        logger.info("===== toggleFavorite 执行 =====");
        logger.info("userId: {}, articleId: {}", userId, articleId);

        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);

        Favorite existing = this.getOne(wrapper);
        logger.info("existing: {}", existing != null ? existing.getId() : "null");

        if (existing != null) {
            // 已收藏，取消收藏
            logger.info("取消收藏, id={}", existing.getId());
            boolean result = this.removeById(existing.getId());
            logger.info("取消收藏结果: {}", result);
            return result;
        } else {
            // 未收藏，添加收藏
            logger.info("新增收藏");
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setArticleId(articleId);
            favorite.setCreateTime(LocalDateTime.now());
            boolean result = this.save(favorite);
            logger.info("新增收藏结果: {}, id: {}", result, favorite.getId());
            return result;
        }
    }

    @Override
    public boolean isFavorited(Integer userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);
        return this.count(wrapper) > 0;
    }

    @Override
    public int getFavoriteCount(Integer articleId) {
        if (articleId == null) {
            return 0;
        }
        return favoriteMapper.countByArticleId(articleId);
    }

    @Override
    public IPage<ArticleVO> getUserFavorites(Integer userId, Integer page, Integer size) {
        logger.info("查询用户收藏: userId={}, page={}, size={}", userId, page, size);

        // 先查询用户收藏的商品ID列表（按收藏时间降序）
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);

        List<Favorite> favorites = this.list(wrapper);
        logger.info("用户收藏记录数: {}", favorites.size());

        // 创建分页对象
        Page<ArticleVO> pageParam = new Page<>(page, size);

        if (favorites.isEmpty()) {
            // 没有收藏，返回空分页
            pageParam.setTotal(0);
            pageParam.setRecords(new ArrayList<>());
            return pageParam;
        }

        // 获取商品ID列表
        List<Integer> articleIds = favorites.stream()
                .map(Favorite::getArticleId)
                .collect(java.util.stream.Collectors.toList());

        logger.info("收藏的商品ID列表: {}", articleIds);

        // ========== 使用 JOIN 查询获取作者和分类名称 ==========
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        // 使用 IN 查询商品ID列表
        queryWrapper.in("a.id", articleIds);
        queryWrapper.eq("a.status", 1);  // 已发布
        queryWrapper.eq("a.product_status", 0);  // 在售
        queryWrapper.orderByDesc("a.published_at");

        // 使用 ArticleMapper 的 JOIN 查询方法
        // 注意：selectArticleVOPage 返回的是 ArticleVO，且已包含 authorName 和 categoryName
        Page<ArticleVO> articleVOPage = new Page<>(page, size);
        IPage<ArticleVO> result = articleMapper.selectArticleVOPage(articleVOPage, queryWrapper);

        logger.info("查询到的商品数: {}", result.getRecords().size());

        return result;
    }
}