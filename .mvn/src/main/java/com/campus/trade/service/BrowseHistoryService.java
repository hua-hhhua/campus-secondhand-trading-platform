package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.entity.BrowseHistory;

import java.util.List;

public interface BrowseHistoryService extends IService<BrowseHistory> {

    /**
     * 记录浏览历史（同一商品只保留最近一条）
     */
    void recordBrowse(Integer userId, Integer articleId);

    /**
     * 获取用户的浏览历史列表（含商品详情）
     */
    List<ArticleVO> getBrowseHistoryByUserId(Integer userId);

    /**
     * 清空用户的浏览历史
     */
    void clearBrowseHistory(Integer userId);
}