package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.constant.ArticleStatus;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleResultMapVO;
import com.campus.trade.entity.ArticleTag;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.mapper.ArticleTagMapper;
import com.campus.trade.mapper.TagMapper;
import com.campus.trade.service.ArticleNotificationProducer;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.AsyncService;
import com.campus.trade.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 【商品模块-服务实现层】
 * 商品服务实现类
 * 实现商品模块的核心业务逻辑，包括商品的增删改查、发布状态管理、标签关联、库存管理、Redis缓存等功能
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleNotificationProducer articleNotificationProducer;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private CacheService cacheService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 分页查询所有文章（管理后台使用）
     *
     * @param page    页码
     * @param size    每页数量
     * @param keyword 关键词（搜索标题或内容）
     * @return 分页结果
     */
    @Override
    public IPage<Article> findAllPage(Integer page, Integer size, String keyword) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Article::getTitle, keyword).or().like(Article::getContent, keyword);
        }
        wrapper.orderByDesc(Article::getCreateTime);

        return this.page(pageParam, wrapper);
    }

    /**
     * 分页查询我的文章（普通用户使用）
     *
     * @param page    页码
     * @param size    每页数量
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 分页结果
     */
    @Override
    public IPage<Article> findMyPage(Integer page, Integer size, String keyword, Integer userId) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Article::getUserId, userId);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Article::getTitle, keyword).or().like(Article::getContent, keyword);
        }
        wrapper.orderByDesc(Article::getCreateTime);

        return this.page(pageParam, wrapper);
    }

    /**
     * 增加浏览量
     * 每次访问文章详情页时调用，同步更新数据库并异步记录统计信息
     *
     * @param id 文章ID
     */
    @Override
    public void incrementViewCount(Integer id) {
        Article article = this.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            this.updateById(article);
            asyncService.asyncUpdateArticleStats(id, "incrementView");
        }
    }

    // ========== 重写MyBatis-Plus方法，统一清除缓存 ==========

    /**
     * 保存商品（重写）
     * 保存成功后自动清除所有商品列表缓存
     *
     * @param article 商品实体
     * @return 是否保存成功
     */
    @Override
    public boolean save(Article article) {
        boolean result = super.save(article);
        if (result) {
            cacheService.deleteAllArticleList();
            System.out.println("【Redis缓存】新增商品后清除商品列表缓存 - ID: " + article.getId());
        }
        return result;
    }

    /**
     * 【商品模块-更新商品】
     * 更新商品（重写）
     * 更新成功后自动清除商品列表缓存和该商品详情缓存
     *
     * @param article 商品实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(Article article) {
        boolean result = super.updateById(article);
        if (result) {
            cacheService.deleteAllArticleList();
            if (article.getId() != null) {
                cacheService.deleteArticleDetail(article.getId());
            }
            System.out.println("【Redis缓存】更新商品后清除缓存 - ID: " + article.getId());
        }
        return result;
    }

    /**
     * 删除商品（重写，传入实体）
     * 删除成功后自动清除商品列表缓存和该商品详情缓存
     *
     * @param article 商品实体
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Article article) {
        boolean result = super.removeById(article);
        if (result) {
            cacheService.deleteAllArticleList();
            if (article.getId() != null) {
                cacheService.deleteArticleDetail(article.getId());
            }
            System.out.println("【Redis缓存】删除商品后清除缓存 - ID: " + article.getId());
        }
        return result;
    }

    /**
     * 删除商品（重写，传入ID）
     * 删除成功后自动清除商品列表缓存和该商品详情缓存
     *
     * @param id 商品ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            cacheService.deleteAllArticleList();
            cacheService.deleteArticleDetail((Integer) id);
            System.out.println("【Redis缓存】删除商品后清除缓存 - ID: " + id);
        }
        return result;
    }

    // ========== 消息中间件相关方法 ==========

    /**
     * 【商品模块-发布商品】
     * 发布文章
     * 设置默认值后保存，并异步发送通知
     *
     * @param article 文章实体
     * @return 是否发布成功
     */
    @Override
    public boolean publishArticle(Article article) {
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getStatus() == null) {
            article.setStatus(ArticleStatus.PUBLISHED);
        }
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        boolean result = this.save(article);

        if (result) {
            cacheService.deleteAllArticleList();
            System.out.println("【Redis缓存】发布商品后清除商品列表缓存");
            asyncService.asyncSendArticleNotification(article, "publish");
        }

        return result;
    }

    /**
     * 【商品模块-更新商品】
     * 更新文章
     * 更新成功后清除缓存并异步发送通知
     *
     * @param article 文章实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateArticle(Article article) {
        boolean result = this.updateById(article);

        if (result) {
            cacheService.deleteAllArticleList();
            cacheService.deleteArticleDetail(article.getId());
            System.out.println("【Redis缓存】更新商品后清除缓存 - ID: " + article.getId());
            asyncService.asyncSendArticleNotification(article, "update");
        }

        return result;
    }

    /**
     * 删除文章
     * 删除成功后清除缓存并异步发送删除通知
     *
     * @param id 文章ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteArticle(Integer id) {
        Article article = this.getById(id);
        boolean result = this.removeById(id);

        if (result && article != null) {
            cacheService.deleteAllArticleList();
            cacheService.deleteArticleDetail(id);
            System.out.println("【Redis缓存】删除商品后清除缓存 - ID: " + id);
            String currentUsername = getCurrentUsername();
            asyncService.asyncSendDeleteNotification(id, article.getTitle(), currentUsername);
        }

        return result;
    }

    // ========== 定时任务相关方法 ==========

    /**
     * 获取已发布的文章列表（定时任务使用）
     * 查询状态为已发布且发布时间不超过当前时间的文章
     *
     * @return 已发布的文章列表
     */
    @Override
    public List<Article> getPublishedArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .le(Article::getPublishedAt, LocalDateTime.now())
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishedAt);
        return this.list(wrapper);
    }

    /**
     * 获取待发布的定时文章
     * 查询状态为定时发布且发布时间已到的文章
     *
     * @return 待发布的文章列表
     */
    @Override
    public List<Article> getScheduledArticlesToPublish() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatus.SCHEDULED)
                .le(Article::getPublishedAt, LocalDateTime.now());
        return this.list(wrapper);
    }

    private static final Object PUBLISH_LOCK = new Object();

    /**
     * 发布定时文章（定时任务调用）
     * 使用synchronized防止并发执行，保证事务一致性
     *
     * @param now 当前时间（Asia/Shanghai时区）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishScheduledArticles(LocalDateTime now) {
        synchronized (PUBLISH_LOCK) {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getStatus, ArticleStatus.SCHEDULED)
                    .le(Article::getPublishedAt, now);

            List<Article> scheduledArticles = this.list(wrapper);

            if (scheduledArticles.isEmpty()) {
                return;
            }

            for (Article article : scheduledArticles) {
                article.setStatus(ArticleStatus.PUBLISHED);
                article.setProductStatus(0);
                article.setUpdateTime(now);
                this.updateById(article);
                System.out.println("【定时发布】文章已发布 - ID: " + article.getId() + ", 标题: " + article.getTitle());
            }

            // 定时发布后清除缓存
            cacheService.deleteAllArticleList();
            System.out.println("【Redis缓存】定时发布商品后清除商品列表缓存，数量: " + scheduledArticles.size());
        }
    }

    // ========== 新增方法 ==========

    /**
     * 【商品模块-保存或更新商品】
     * 保存或更新文章（核心方法）
     * 根据文章ID判断是新增还是更新，设置默认值并校验，处理标签关联
     *
     * @param article       文章实体
     * @param currentUserId 当前用户ID
     * @return 是否保存成功
     */
    @Override
    @Transactional
    public boolean saveOrUpdateArticle(Article article, Integer currentUserId) {
        article.setUserId(currentUserId);

        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }

        if (article.getSendEmail() == null) {
            article.setSendEmail(0);
        }

        if (article.getAllowComment() == null) {
            article.setAllowComment(1);
        }

        if (article.getIsTop() == null) {
            article.setIsTop(0);
        }

        if (!validateArticleForPublish(article)) {
            System.out.println("【文章保存】校验失败 - 标题: " + article.getTitle());
            return false;
        }

        boolean result;
        if (article.getId() == null) {
            result = doCreateArticle(article);
        } else {
            result = doUpdateArticle(article);
        }

        // ========== 保存标签关联 ==========
        if (result && article.getId() != null) {
            saveArticleTags(article.getId(), article.getTagIds());
        }

        return result;
    }

    /**
     * 创建文章（内部方法）
     * 设置创建时间和更新时间，处理定时发布时间，保存并清除缓存
     *
     * @param article 文章实体
     * @return 是否创建成功
     */
    private boolean doCreateArticle(Article article) {
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        // 如果状态为空或为null，默认设置为草稿
        if (article.getStatus() == null) {
            article.setStatus(ArticleStatus.DRAFT);
        }

        // 如果是定时发布但没有设置发布时间，自动设置为1小时后
        if (article.getStatus() == ArticleStatus.SCHEDULED && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now().plusHours(1));
            System.out.println("【发布时间处理】定时发布未设置时间，已自动设置为1小时后");
        }

        boolean result = this.save(article);

        if (result) {
            // 清除商品列表缓存
            cacheService.deleteAllArticleList();
            System.out.println("【Redis缓存】创建商品后清除商品列表缓存");

            if (article.getStatus() == ArticleStatus.SCHEDULED) {
                System.out.println("【文章保存】定时发布文章已保存 - ID: " + article.getId()
                        + ", 标题: " + article.getTitle()
                        + ", 定时发布时间: " + article.getPublishedAt());
            } else if (article.getStatus() == ArticleStatus.PUBLISHED) {
                System.out.println("【文章保存】文章已发布 - ID: " + article.getId() + ", 标题: " + article.getTitle());
                if (article.getSendEmail() != null && article.getSendEmail() == 1) {
                    asyncService.asyncSendArticleNotification(article, "publish");
                    System.out.println("【邮件通知】立即发布文章已发送邮件通知 - ID: " + article.getId());
                } else {
                    System.out.println("【邮件通知】立即发布文章未勾选邮件通知，跳过发送 - ID: " + article.getId());
                }
            } else {
                System.out.println("【文章保存】草稿已保存 - ID: " + article.getId() + ", 标题: " + article.getTitle());
            }
        }

        return result;
    }

    /**
     * 【商品模块-更新商品（内部）】
     * 更新文章（内部方法）
     * 设置更新时间，保留定时发布时间，更新并清除缓存
     *
     * @param article 文章实体
     * @return 是否更新成功
     */
    private boolean doUpdateArticle(Article article) {
        article.setUpdateTime(LocalDateTime.now());

        // 如果是定时发布但没有设置发布时间，保留原有的发布时间
        if (article.getStatus() == ArticleStatus.SCHEDULED && article.getPublishedAt() == null) {
            Article existing = this.getById(article.getId());
            if (existing != null && existing.getPublishedAt() != null) {
                article.setPublishedAt(existing.getPublishedAt());
                System.out.println("【发布时间处理】保留原有定时发布时间: " + article.getPublishedAt());
            }
        }

        boolean result = this.updateById(article);

        if (result) {
            // 清除商品列表缓存
            cacheService.deleteAllArticleList();
            // 清除该商品详情缓存
            cacheService.deleteArticleDetail(article.getId());
            System.out.println("【Redis缓存】更新商品后清除缓存 - ID: " + article.getId());

            System.out.println("【文章保存】文章已更新 - ID: " + article.getId() + ", 标题: " + article.getTitle());
            asyncService.asyncSendArticleNotification(article, "update");
        }

        return result;
    }

    /**
     * 【商品模块-验证商品发布】
     * 验证文章是否可以发布
     * 检查标题、内容是否非空，定时发布时检查发布时间是否有效
     *
     * @param article 文章实体
     * @return 是否验证通过
     */
    @Override
    public boolean validateArticleForPublish(Article article) {
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            System.out.println("【文章校验失败】文章标题不能为空");
            return false;
        }

        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            System.out.println("【文章校验失败】文章内容不能为空");
            return false;
        }

        if (article.getStatus() != null && article.getStatus() == ArticleStatus.SCHEDULED) {
            if (article.getPublishedAt() == null) {
                System.out.println("【文章校验失败】定时发布必须设置发布时间");
                return false;
            }
            // 允许发布时间等于当前时间（允许立即定时发布）
            // 只检查发布时间不能早于当前时间
            if (article.getPublishedAt().isBefore(LocalDateTime.now())) {
                System.out.println("【文章校验失败】定时发布时间不能早于当前时间");
                return false;
            }
        }

        return true;
    }

    /**
     * 分页查询已发布的文章
     * 查询状态为已发布且发布时间不超过当前时间的文章，支持关键词搜索
     *
     * @param page    页码
     * @param size    每页数量
     * @param keyword 关键词
     * @return 分页结果
     */
    @Override
    public IPage<Article> getPublishedArticlesPage(Integer page, Integer size, String keyword) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .le(Article::getPublishedAt, LocalDateTime.now());

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getContent, keyword));
        }

        wrapper.orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishedAt);

        return this.page(pageParam, wrapper);
    }

    // ========== 多表查询方法（支持时间范围） ==========

    /**
     * 分页查询文章VO（带作者、分类等信息）
     * 使用JOIN查询，返回包含作者昵称、头像、分类名称等扩展信息的文章列表
     *
     * @param page         页码
     * @param size         每页数量
     * @param keyword      关键词
     * @param statusFilter 状态筛选（0=草稿，1=已发布，2=定时发布）
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 分页结果
     */
    @Override
    public IPage<ArticleVO> getArticleVOPage(Integer page, Integer size, String keyword, Integer statusFilter,
            LocalDateTime startTime, LocalDateTime endTime) {
        Page<ArticleVO> pageParam = new Page<>(page, size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("a.title", keyword).or().like("a.content", keyword));
        }

        if (statusFilter != null) {
            wrapper.eq("a.status", statusFilter);
        }

        if (startTime != null) {
            wrapper.ge("a.create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("a.create_time", endTime);
        }

        wrapper.orderByDesc("a.create_time");

        return baseMapper.selectArticleVOPage(pageParam, wrapper);
    }

    // ========== 按用户ID查询商品 ==========

    /**
     * 按用户ID分页查询文章VO
     * 查询指定用户发布的文章，包含作者、分类等扩展信息
     *
     * @param page         页码
     * @param size         每页数量
     * @param keyword      关键词
     * @param statusFilter 状态筛选
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param userId       用户ID
     * @return 分页结果
     */
    @Override
    public IPage<ArticleVO> getArticleVOPageByUserId(Integer page, Integer size, String keyword,
            Integer statusFilter, LocalDateTime startTime,
            LocalDateTime endTime, Integer userId) {
        Page<ArticleVO> pageParam = new Page<>(page, size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        // ========== 关键：按用户ID过滤 ==========
        wrapper.eq("a.user_id", userId);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("a.title", keyword).or().like("a.content", keyword));
        }

        if (statusFilter != null) {
            wrapper.eq("a.status", statusFilter);
        }

        if (startTime != null) {
            wrapper.ge("a.create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("a.create_time", endTime);
        }

        wrapper.orderByDesc("a.create_time");

        return baseMapper.selectArticleVOPage(pageParam, wrapper);
    }

    // ========== 方案二：JOIN查询（首页）实现 ==========

    /**
     * 首页查询已发布的文章VO
     * 查询首页展示的商品列表，只查询在售商品，包含标签信息
     *
     * @param keyword 关键词
     * @return 文章VO列表
     */
    @Override
    public List<ArticleVO> getPublishedArticleVOsForHome(String keyword) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        // 只查询已发布的文章
        wrapper.eq("a.status", ArticleStatus.PUBLISHED);

        // ========== 只查询在售商品 ==========
        wrapper.eq("a.product_status", 0); // 0=在售

        // 发布时间不超过当前时间
        wrapper.le("a.published_at", LocalDateTime.now());

        // 关键字搜索
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("a.title", keyword)
                    .or()
                    .like("a.content", keyword)
                    .or()
                    .like("a.summary", keyword));
        }

        // 排序：置顶降序、发布时间降序
        wrapper.orderByDesc("a.is_top")
                .orderByDesc("a.published_at");

        Page<ArticleVO> page = new Page<>(1, 100);
        IPage<ArticleVO> result = baseMapper.selectArticleVOPage(page, wrapper);

        return result.getRecords();
    }

    // ========== 方案三：MyBatis ResultMap 关联查询（懒加载）实现 ==========

    /**
     * 分页查询文章ResultMap VO（懒加载）
     * 使用MyBatis ResultMap关联查询，支持懒加载作者和分类信息
     *
     * @param page         页码
     * @param size         每页数量
     * @param keyword      关键词
     * @param statusFilter 状态筛选
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 分页结果
     */
    @Override
    public IPage<ArticleResultMapVO> getArticleResultMapVOsByPage(Integer page, Integer size, String keyword,
            Integer statusFilter, LocalDateTime startTime,
            LocalDateTime endTime) {
        IPage<Article> pageParam = new Page<>(page, size);
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }

        if (statusFilter != null) {
            queryWrapper.eq("status", statusFilter);
        }

        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }

        queryWrapper.orderByDesc("create_time");

        System.out.println("========== 方案三：MyBatis ResultMap 懒加载查询 ==========");
        System.out.println("执行主查询：SELECT * FROM article WHERE ...");

        return baseMapper.selectArticleResultMapVOs(pageParam, queryWrapper);
    }

    // ========== 标签关联方法 ==========

    /**
     * 获取文章的标签ID列表
     * 查询文章关联的所有标签ID
     *
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    @Override
    public List<Integer> getTagIdsByArticleId(Integer articleId) {
        return articleTagMapper.selectTagIdsByArticleId(articleId);
    }

    /**
     * 【商品模块-保存商品标签关联】
     * 保存文章标签关联
     * 先删除原有标签关联，再保存新的标签关联，最后清除缓存
     *
     * @param articleId 文章ID
     * @param tagIds    标签ID列表
     */
    @Override
    @Transactional
    public void saveArticleTags(Integer articleId, List<Integer> tagIds) {
        System.out.println("========== saveArticleTags DEBUG ==========");
        System.out.println("商品ID: " + articleId);
        System.out.println("标签IDs: " + tagIds);

        // 先删除旧的关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        int deleteCount = articleTagMapper.delete(wrapper);
        System.out.println("删除旧关联数量: " + deleteCount);

        // 插入新的关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Integer tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
                System.out.println("插入新关联: article_id=" + articleId + ", tag_id=" + tagId);
            }
        }

        // 清除商品列表缓存
        cacheService.deleteAllArticleList();
        System.out.println("【Redis缓存】保存标签后清除商品列表缓存");

        // 清除商品详情缓存
        cacheService.deleteArticleDetail(articleId);
        System.out.println("【Redis缓存】保存标签后清除商品详情缓存: " + articleId);
        System.out.println("========== saveArticleTags 完成 ==========");
    }

    // ========== 图片上传方法 ==========

    /**
     * 【商品模块-上传封面图片】
     * 上传封面图片
     * 将图片保存到服务器指定目录（uploads/articles/），生成UUID文件名，并返回图片访问路径
     *
     * @param file   图片文件
     * @param userId 用户ID（用于生成存储路径）
     * @return 图片访问路径（/uploads/articles/xxx.jpg）
     */
    @Override
    public String uploadCoverImage(MultipartFile file, Integer userId) {
        try {
            // 创建目录
            Path uploadDir = Paths.get(uploadPath, "articles");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            Files.write(filePath, file.getBytes());

            // 返回访问路径
            String imagePath = "/uploads/articles/" + filename;

            return imagePath;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取内容摘要（内部方法）
     * 如果内容超过200字符，截取前200字符
     *
     * @param content 内容
     * @return 摘要
     */
    private String getSummary(String content) {
        if (content == null)
            return "";
        return content.length() > 200 ? content.substring(0, 200) : content;
    }

    /**
     * 获取当前用户名（内部方法）
     * 从Spring Security上下文获取当前登录用户的用户名
     *
     * @return 用户名（未登录返回"system"）
     */
    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception e) {
            // 忽略
        }
        return "system";
    }

    // ========== 多条件查询（关键词 + 学校 + 分类） ==========

    /**
     * 多条件查询文章（关键词 + 学校 + 分类）
     * 支持Redis缓存，首页搜索功能使用，查询结果包含标签名称
     *
     * @param keyword    关键词（搜索标题或内容）
     * @param schoolId   学校ID
     * @param categoryId 分类ID
     * @return 文章VO列表
     */
    @Override
    public List<ArticleVO> getArticlesByConditions(String keyword, Integer schoolId, Integer categoryId) {
        String cacheKey = buildArticleListCacheKey(keyword, schoolId, categoryId);

        try {
            List<ArticleVO> cachedList = cacheService.getArticleList(cacheKey);
            if (cachedList != null && !cachedList.isEmpty()) {
                System.out.println("【Redis缓存命中】商品列表 - " + cacheKey + "，数量: " + cachedList.size());
                return cachedList;
            }
        } catch (Exception e) {
            System.out.println("【Redis缓存】获取商品列表异常，清除缓存并从数据库查询: " + e.getMessage());
            cacheService.deleteArticleList(cacheKey);
        }

        System.out.println("【Redis缓存未命中】商品列表 - " + cacheKey + "，从数据库查询");

        try {
            QueryWrapper<Article> wrapper = new QueryWrapper<>();

            wrapper.eq("a.status", ArticleStatus.PUBLISHED);
            wrapper.eq("a.product_status", 0);
            wrapper.le("a.published_at", LocalDateTime.now(ZoneId.of("Asia/Shanghai")));

            if (StrUtil.isNotBlank(keyword)) {
                wrapper.and(w -> w.like("a.title", keyword)
                        .or()
                        .like("a.content", keyword));
            }

            if (schoolId != null && schoolId > 0) {
                wrapper.eq("a.school_id", schoolId);
            }

            if (categoryId != null && categoryId > 0) {
                wrapper.eq("a.category_id", categoryId);
            }

            wrapper.orderByDesc("a.is_top")
                    .orderByDesc("a.published_at");

            Page<ArticleVO> page = new Page<>(1, 100);
            IPage<ArticleVO> result = baseMapper.selectArticleVOPage(page, wrapper);
            List<ArticleVO> articles = result.getRecords();

            if (articles != null && !articles.isEmpty()) {
                for (ArticleVO articleVO : articles) {
                    try {
                        List<Integer> tagIds = articleTagMapper.selectTagIdsByArticleId(articleVO.getId());
                        if (tagIds != null && !tagIds.isEmpty()) {
                            LambdaQueryWrapper<com.campus.trade.entity.Tag> tagWrapper = new LambdaQueryWrapper<>();
                            tagWrapper.in(com.campus.trade.entity.Tag::getId, tagIds);
                            List<com.campus.trade.entity.Tag> tags = tagMapper.selectList(tagWrapper);
                            List<String> tagNames = tags.stream()
                                    .map(com.campus.trade.entity.Tag::getName)
                                    .toList();
                            articleVO.setTagNames(tagNames);
                        }
                    } catch (Exception e) {
                        System.out.println("【标签查询】获取商品标签失败，商品ID: " + articleVO.getId() + ", 错误: " + e.getMessage());
                    }
                }

                try {
                    cacheService.setArticleList(cacheKey, articles);
                    System.out.println("【Redis缓存】商品列表已存入缓存 - " + cacheKey + "，数量: " + articles.size());
                } catch (Exception e) {
                    System.out.println("【Redis缓存】设置商品列表缓存失败: " + e.getMessage());
                }
            }

            return articles != null ? articles : new java.util.ArrayList<>();
        } catch (Exception e) {
            System.out.println("【数据库查询】获取商品列表失败: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 构建商品列表缓存key（内部方法）
     * 根据关键词、学校ID、分类ID生成唯一缓存key
     *
     * @param keyword    关键词
     * @param schoolId   学校ID
     * @param categoryId 分类ID
     * @return 缓存key
     */
    private String buildArticleListCacheKey(String keyword, Integer schoolId, Integer categoryId) {
        StringBuilder key = new StringBuilder("home:");
        if (StrUtil.isNotBlank(keyword)) {
            key.append("kw_").append(keyword.hashCode()).append(":");
        }
        if (schoolId != null && schoolId > 0) {
            key.append("school_").append(schoolId).append(":");
        }
        if (categoryId != null && categoryId > 0) {
            key.append("cat_").append(categoryId);
        }
        return key.toString();
    }

    // ========== 库存管理方法 ==========

    /**
     * 【商品模块-扣减库存】
     * 扣减库存
     * 下单成功后调用，扣减商品库存，库存为0时自动下架商品
     *
     * @param articleId 文章ID
     * @param quantity  扣减数量
     * @return 是否扣减成功（库存充足返回true，库存不足返回false）
     */
    @Override
    @Transactional
    public boolean deductStock(Integer articleId, Integer quantity) {
        // 1. 查询商品
        Article article = this.getById(articleId);
        if (article == null) {
            System.out.println("【库存扣减】商品不存在 - articleId: " + articleId);
            return false;
        }

        // 2. 校验库存是否充足
        if (article.getStock() < quantity) {
            System.out.println(
                    "【库存扣减】库存不足 - articleId: " + articleId + ", 当前库存: " + article.getStock() + ", 需要: " + quantity);
            return false;
        }

        // 3. 扣减库存
        article.setStock(article.getStock() - quantity);
        System.out.println("【库存扣减】扣减成功 - articleId: " + articleId + ", 剩余库存: " + article.getStock());

        // 4. 库存为0时自动下架
        if (article.getStock() == 0) {
            article.setProductStatus(2); // 2=已下架
            System.out.println("【库存扣减】库存为0，商品已自动下架 - articleId: " + articleId);
        }

        // 5. 更新数据库
        boolean result = this.updateById(article);
        if (result) {
            cacheService.deleteAllArticleList();
            cacheService.deleteArticleDetail(articleId);
            System.out.println("【Redis缓存】库存扣减后清除缓存 - ID: " + articleId);
        }
        return result;
    }
}