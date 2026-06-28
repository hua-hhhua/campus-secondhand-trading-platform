package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleResultMapVO;
import com.campus.trade.entity.ArticleVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleService extends IService<Article> {

    // ========== 基础分页查询 ==========

    /**
     * 分页查询所有文章
     */
    IPage<Article> findAllPage(Integer page, Integer size, String keyword);

    /**
     * 分页查询我的文章
     */
    IPage<Article> findMyPage(Integer page, Integer size, String keyword, Integer userId);

    /**
     * 分页查询已发布的文章
     */
    IPage<Article> getPublishedArticlesPage(Integer page, Integer size, String keyword);

    // ========== 文章操作 ==========

    /**
     * 发布文章
     */
    boolean publishArticle(Article article);

    /**
     * 更新文章
     */
    boolean updateArticle(Article article);

    /**
     * 删除文章
     */
    boolean deleteArticle(Integer id);

    /**
     * 保存或更新文章
     */
    boolean saveOrUpdateArticle(Article article, Integer currentUserId);

    /**
     * 验证文章是否可以发布
     */
    boolean validateArticleForPublish(Article article);

    // ========== 统计和查询 ==========

    /**
     * 增加浏览量
     */
    void incrementViewCount(Integer id);

    /**
     * 获取已发布的文章列表（定时任务使用）
     */
    List<Article> getPublishedArticles();

    /**
     * 获取待发布的定时文章
     */
    List<Article> getScheduledArticlesToPublish();

    /**
     * 发布定时文章
     */
    void publishScheduledArticles(LocalDateTime now);

    // ========== 多表查询（VO） ==========

    /**
     * 分页查询文章VO（带作者、分类等信息）
     */
    IPage<ArticleVO> getArticleVOPage(Integer page, Integer size, String keyword,
                                      Integer statusFilter, LocalDateTime startTime,
                                      LocalDateTime endTime);

    /**
     * 按用户ID分页查询文章VO
     */
    IPage<ArticleVO> getArticleVOPageByUserId(Integer page, Integer size, String keyword,
                                              Integer statusFilter, LocalDateTime startTime,
                                              LocalDateTime endTime, Integer userId);

    /**
     * 首页查询已发布的文章VO
     */
    List<ArticleVO> getPublishedArticleVOsForHome(String keyword);

    /**
     * 多条件查询文章（关键词 + 学校 + 分类）
     */
    List<ArticleVO> getArticlesByConditions(String keyword, Integer schoolId, Integer categoryId);

    // ========== ResultMap 查询 ==========

    /**
     * 分页查询文章ResultMap VO（懒加载）
     */
    IPage<ArticleResultMapVO> getArticleResultMapVOsByPage(Integer page, Integer size, String keyword,
                                                           Integer statusFilter, LocalDateTime startTime,
                                                           LocalDateTime endTime);

    // ========== 标签关联 ==========

    /**
     * 获取文章的标签ID列表
     */
    List<Integer> getTagIdsByArticleId(Integer articleId);

    /**
     * 保存文章标签关联
     */
    void saveArticleTags(Integer articleId, List<Integer> tagIds);

    // ========== 图片上传 ==========

    /**
     * 上传封面图片
     */
    String uploadCoverImage(MultipartFile file, Integer userId);

    // ========== 库存管理 ==========

    /**
     * 扣减库存
     */
    boolean deductStock(Integer articleId, Integer quantity);
}