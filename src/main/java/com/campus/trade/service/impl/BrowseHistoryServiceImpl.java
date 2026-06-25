package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.BrowseHistory;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.mapper.BrowseHistoryMapper;
import com.campus.trade.service.BrowseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    @Transactional
    public void recordBrowse(Integer userId, Integer articleId) {
        if (userId == null || articleId == null) {
            return;
        }

        // 查询是否已有该商品的浏览记录
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getArticleId, articleId);

        BrowseHistory existing = this.getOne(wrapper);

        if (existing != null) {
            // 已有记录，只更新时间
            existing.setBrowseTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);
        } else {
            // 没有记录，插入新记录
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setArticleId(articleId);
            history.setBrowseTime(LocalDateTime.now());
            history.setUpdateTime(LocalDateTime.now());
            this.save(history);
        }
    }

    @Override
    public List<ArticleVO> getBrowseHistoryByUserId(Integer userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        // 查询用户的所有浏览记录（按浏览时间倒序）
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId)
                .orderByDesc(BrowseHistory::getBrowseTime);

        List<BrowseHistory> histories = this.list(wrapper);

        if (histories.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取商品ID列表
        List<Integer> articleIds = histories.stream()
                .map(BrowseHistory::getArticleId)
                .toList();

        // 查询商品信息（使用 JOIN 获取作者和分类名称）
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.in("a.id", articleIds);
        queryWrapper.eq("a.status", 1);
        queryWrapper.orderByDesc("a.published_at");

        // 使用 ArticleMapper 的 JOIN 查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ArticleVO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 100);
        List<ArticleVO> voList = articleMapper.selectArticleVOPage(page, queryWrapper).getRecords();

        // 按浏览时间排序（最新浏览的在前）
        // 由于查询结果可能打乱顺序，重新按 histories 顺序排列
        List<ArticleVO> result = new ArrayList<>();
        for (BrowseHistory history : histories) {
            for (ArticleVO vo : voList) {
                if (vo.getId().equals(history.getArticleId())) {
                    result.add(vo);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void clearBrowseHistory(Integer userId) {
        if (userId == null) {
            return;
        }
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId);
        this.remove(wrapper);
    }
}