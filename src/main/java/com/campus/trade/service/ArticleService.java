package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleResultMapVO;
import com.campus.trade.entity.ArticleVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【商品模块-服务接口层】
 * 商品服务接口
 * 定义商品模块的核心业务逻辑方法，包括商品的增删改查、发布状态管理、标签关联、库存管理等
 */
public interface ArticleService extends IService<Article> {

    // ========== 基础分页查询 ==========

    /**
     * 分页查询所有文章（管理后台使用）
     *
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词（搜索标题或内容）
     * @return 分页结果
     */
    IPage<Article> findAllPage(Integer page, Integer size, String keyword);

    /**
     * 分页查询我的文章（普通用户使用）
     *
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Article> findMyPage(Integer page, Integer size, String keyword, Integer userId);

    /**
     * 分页查询已发布的文章
     *
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词
     * @return 分页结果
     */
    IPage<Article> getPublishedArticlesPage(Integer page, Integer size, String keyword);

    // ========== 文章操作 ==========

    /**
     * 发布文章
     * 将文章状态从草稿或定时发布改为已发布
     *
     * @param article 文章实体
     * @return 是否发布成功
     */
    boolean publishArticle(Article article);

    /**
     * 【商品模块-更新商品】
     * 更新文章
     * 修改文章信息
     *
     * @param article 文章实体
     * @return 是否更新成功
     */
    boolean updateArticle(Article article);

    /**
     * 删除文章
     * 物理删除文章及其关联的标签关系
     *
     * @param id 文章ID
     * @return 是否删除成功
     */
    boolean deleteArticle(Integer id);

    /**
     * 保存或更新文章
     * 根据文章ID判断是新增还是更新，并处理发布时间和状态逻辑
     *
     * @param article 文章实体
     * @param currentUserId 当前用户ID
     * @return 是否保存成功
     */
    boolean saveOrUpdateArticle(Article article, Integer currentUserId);

    /**
     * 【商品模块-验证商品发布】
     * 验证文章是否可以发布
     * 检查必填字段是否完整
     *
     * @param article 文章实体
     * @return 是否验证通过
     */
    boolean validateArticleForPublish(Article article);

    // ========== 统计和查询 ==========

    /**
     * 增加浏览量
     * 每次访问文章详情页时调用
     *
     * @param id 文章ID
     */
    void incrementViewCount(Integer id);

    /**
     * 获取已发布的文章列表（定时任务使用）
     *
     * @return 已发布的文章列表
     */
    List<Article> getPublishedArticles();

    /**
     * 【商品模块-获取待发布定时商品】
     * 获取待发布的定时文章
     * 查询状态为定时发布且发布时间已到的文章
     *
     * @return 待发布的文章列表
     */
    List<Article> getScheduledArticlesToPublish();

    /**
     * 发布定时文章
     * 定时任务调用，将到达发布时间的定时文章改为已发布状态
     *
     * @param now 当前时间（Asia/Shanghai时区）
     */
    void publishScheduledArticles(LocalDateTime now);

    // ========== 多表查询（VO） ==========

    /**
     * 分页查询文章VO（带作者、分类等信息）
     * 返回包含作者昵称、头像、分类名称等扩展信息的文章列表
     *
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词
     * @param statusFilter 状态筛选（0=草稿，1=已发布，2=定时发布）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<ArticleVO> getArticleVOPage(Integer page, Integer size, String keyword,
                                      Integer statusFilter, LocalDateTime startTime,
                                      LocalDateTime endTime);

    /**
     * 按用户ID分页查询文章VO
     * 查询指定用户发布的文章，包含扩展信息
     *
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词
     * @param statusFilter 状态筛选
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<ArticleVO> getArticleVOPageByUserId(Integer page, Integer size, String keyword,
                                              Integer statusFilter, LocalDateTime startTime,
                                              LocalDateTime endTime, Integer userId);

    /**
     * 【商品模块-首页查询已发布商品VO】
     * 首页查询已发布的文章VO
     * 查询首页展示的商品列表，包含标签信息
     *
     * @param keyword 关键词
     * @return 文章VO列表
     */
    List<ArticleVO> getPublishedArticleVOsForHome(String keyword);

    /**
     * 多条件查询文章（关键词 + 学校 + 分类）
     * 支持Redis缓存，首页搜索功能使用
     *
     * @param keyword 关键词（搜索标题或内容）
     * @param schoolId 学校ID
     * @param categoryId 分类ID
     * @return 文章VO列表
     */
    List<ArticleVO> getArticlesByConditions(String keyword, Integer schoolId, Integer categoryId);

    // ========== ResultMap 查询 ==========

    /**
     * 分页查询文章ResultMap VO（懒加载）
     * 使用MyBatis ResultMap关联查询，支持懒加载作者和分类信息
     *
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词
     * @param statusFilter 状态筛选
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<ArticleResultMapVO> getArticleResultMapVOsByPage(Integer page, Integer size, String keyword,
                                                           Integer statusFilter, LocalDateTime startTime,
                                                           LocalDateTime endTime);

    // ========== 标签关联 ==========

    /**
     * 获取文章的标签ID列表
     * 查询文章关联的所有标签ID
     *
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    List<Integer> getTagIdsByArticleId(Integer articleId);

    /**
     * 保存文章标签关联
     * 先删除原有标签关联，再保存新的标签关联
     *
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    void saveArticleTags(Integer articleId, List<Integer> tagIds);

    // ========== 图片上传 ==========

    /**
     * 【商品模块-上传封面图片】
     * 上传封面图片
     * 将图片保存到服务器指定目录，并返回图片路径
     *
     * @param file 图片文件
     * @param userId 用户ID（用于生成存储路径）
     * @return 图片访问路径
     */
    String uploadCoverImage(MultipartFile file, Integer userId);

    // ========== 库存管理 ==========

    /**
     * 扣减库存
     * 下单成功后调用，扣减商品库存
     *
     * @param articleId 文章ID
     * @param quantity 扣减数量
     * @return 是否扣减成功（库存充足返回true，库存不足返回false）
     */
    boolean deductStock(Integer articleId, Integer quantity);
}