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

    /**
     * 分页查询文章（管理员查看所有）
     */
    IPage<Article> findAllPage(Integer page, Integer size, String keyword);

    /**
     * 分页查询文章（普通用户/editor只能看自己的）
     */
    IPage<Article> findMyPage(Integer page, Integer size, String keyword, Integer userId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Integer id);

    // ========== 消息中间件相关方法 ==========

    /**
     * 发布文章（发送消息通知）
     */
    boolean publishArticle(Article article);

    /**
     * 更新文章（发送消息通知）
     */
    boolean updateArticle(Article article);

    /**
     * 删除文章（发送消息通知）
     */
    boolean deleteArticle(Integer id);

    // ========== 定时任务相关方法 ==========

    /**
     * 获取已发布的文章列表（用于首页显示）
     */
    List<Article> getPublishedArticles();

    /**
     * 获取定时发布到期的文章列表
     */
    List<Article> getScheduledArticlesToPublish();

    /**
     * 定时发布到期的文章
     */
    void publishScheduledArticles();

    // ========== 新增方法 ==========

    /**
     * 保存或更新文章（统一处理）
     */
    boolean saveOrUpdateArticle(Article article, Integer currentUserId);

    /**
     * 校验文章是否可以发布
     */
    boolean validateArticleForPublish(Article article);

    /**
     * 分页获取已发布的文章列表（用于首页）
     */
    IPage<Article> getPublishedArticlesPage(Integer page, Integer size, String keyword);

    // ========== 多表查询方法 ==========

    /**
     * 分页查询文章VO（包含作者和分类信息）- 支持时间范围查询
     * 方案一：多次查询组装（后台管理用）
     */
    IPage<ArticleVO> getArticleVOPage(Integer page, Integer size, String keyword, Integer statusFilter, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取首页已发布的文章列表（JOIN查询，包含作者和分类名）
     * 方案二：JOIN查询（首页用）
     */
    List<ArticleVO> getPublishedArticleVOsForHome(String keyword);

    // ========== 方案三：MyBatis ResultMap 关联查询（懒加载） ==========

    /**
     * 方案三：MyBatis ResultMap关联查询（支持懒加载）
     * 获取文章列表，包含author和category对象
     */
    IPage<ArticleResultMapVO> getArticleResultMapVOsByPage(Integer page, Integer size, String keyword,
                                                           Integer statusFilter, LocalDateTime startTime,
                                                           LocalDateTime endTime);

    // ========== 标签关联方法 ==========

    /**
     * 获取文章关联的标签ID列表
     */
    List<Integer> getTagIdsByArticleId(Integer articleId);

    /**
     * 保存文章标签关联
     */
    void saveArticleTags(Integer articleId, List<Integer> tagIds);

    // ========== 图片上传方法 ==========

    /**
     * 上传商品封面图
     */
    String uploadCoverImage(MultipartFile file, Integer userId);

    // 在接口中添加
    /**
     * 多条件查询商品（关键词 + 学校 + 分类）
     */
    List<ArticleVO> getArticlesByConditions(String keyword, Integer schoolId, Integer categoryId);
}