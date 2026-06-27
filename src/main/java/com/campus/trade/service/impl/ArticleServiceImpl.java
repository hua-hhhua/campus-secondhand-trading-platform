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
import com.campus.trade.service.ArticleNotificationProducer;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleNotificationProducer articleNotificationProducer;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

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

    @Override
    public void incrementViewCount(Integer id) {
        Article article = this.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            this.updateById(article);
            asyncService.asyncUpdateArticleStats(id, "incrementView");
        }
    }

    // ========== 消息中间件相关方法 ==========

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
            asyncService.asyncSendArticleNotification(article, "publish");
        }

        return result;
    }

    @Override
    public boolean updateArticle(Article article) {
        boolean result = this.updateById(article);

        if (result) {
            asyncService.asyncSendArticleNotification(article, "update");
        }

        return result;
    }

    @Override
    public boolean deleteArticle(Integer id) {
        Article article = this.getById(id);
        boolean result = this.removeById(id);

        if (result && article != null) {
            String currentUsername = getCurrentUsername();
            asyncService.asyncSendDeleteNotification(id, article.getTitle(), currentUsername);
        }

        return result;
    }

    // ========== 定时任务相关方法 ==========

    @Override
    public List<Article> getPublishedArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .le(Article::getPublishedAt, LocalDateTime.now())
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishedAt);
        return this.list(wrapper);
    }

    @Override
    public List<Article> getScheduledArticlesToPublish() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatus.SCHEDULED)
                .le(Article::getPublishedAt, LocalDateTime.now());
        return this.list(wrapper);
    }

    @Override
    public void publishScheduledArticles() {
        List<Article> scheduledArticles = getScheduledArticlesToPublish();

        if (scheduledArticles.isEmpty()) {
            return;
        }

        for (Article article : scheduledArticles) {
            article.setStatus(ArticleStatus.PUBLISHED);
            article.setProductStatus(0);  // 设置为在售
            article.setUpdateTime(LocalDateTime.now());
            this.updateById(article);
            System.out.println("【定时发布】文章已发布 - ID: " + article.getId() + ", 标题: " + article.getTitle() + "，商品状态设为在售");

            if (article.getSendEmail() != null && article.getSendEmail() == 1) {
                asyncService.asyncSendArticleNotification(article, "publish");
                System.out.println("【邮件通知】定时发布文章已发送邮件通知 - ID: " + article.getId());
            } else {
                System.out.println("【邮件通知】定时发布文章未勾选邮件通知，跳过发送 - ID: " + article.getId());
            }
        }

        System.out.println("【定时发布任务执行】共发布了 " + scheduledArticles.size() + " 篇文章");
    }

    // ========== 新增方法 ==========

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

    private boolean doCreateArticle(Article article) {
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        // 直接使用 Controller 已设置好的 status 和 publishedAt，不再额外处理
        // handlePublishedAt(article) 已移除

        boolean result = this.save(article);

        if (result) {
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

    private boolean doUpdateArticle(Article article) {
        article.setUpdateTime(LocalDateTime.now());
        // 直接使用 Controller 已设置好的 status 和 publishedAt，不再额外处理
        // handlePublishedAt(article) 已移除

        boolean result = this.updateById(article);

        if (result) {
            System.out.println("【文章保存】文章已更新 - ID: " + article.getId() + ", 标题: " + article.getTitle());
            asyncService.asyncSendArticleNotification(article, "update");
        }

        return result;
    }

    // ========== 已废弃：不再使用此方法，防止覆盖 status ==========
    // private void handlePublishedAt(Article article) {
    //     此方法已废弃，不再使用
    // }

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
            if (article.getPublishedAt().isBefore(LocalDateTime.now())) {
                System.out.println("【文章校验失败】定时发布时间不能早于当前时间");
                return false;
            }
        }

        return true;
    }

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

    @Override
    public IPage<ArticleVO> getArticleVOPage(Integer page, Integer size, String keyword, Integer statusFilter, LocalDateTime startTime, LocalDateTime endTime) {
        Page<ArticleVO> pageParam = new Page<>(page, size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }

        if (statusFilter != null) {
            wrapper.eq("status", statusFilter);
        }

        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }

        wrapper.orderByDesc("create_time");

        return baseMapper.selectArticleVOPage(pageParam, wrapper);
    }

    // ========== 方案二：JOIN查询（首页）实现 ==========

    @Override
    public List<ArticleVO> getPublishedArticleVOsForHome(String keyword) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        // 只查询已发布的文章
        wrapper.eq("a.status", ArticleStatus.PUBLISHED);

        // ========== 只查询在售商品 ==========
        wrapper.eq("a.product_status", 0);  // 0=在售

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

    @Override
    public List<Integer> getTagIdsByArticleId(Integer articleId) {
        return articleTagMapper.selectTagIdsByArticleId(articleId);
    }

    @Override
    @Transactional
    public void saveArticleTags(Integer articleId, List<Integer> tagIds) {
        // 先删除旧的关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagMapper.delete(wrapper);

        // 插入新的关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Integer tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
    }

    // ========== 图片上传方法 ==========

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

    private String getSummary(String content) {
        if (content == null) return "";
        return content.length() > 200 ? content.substring(0, 200) : content;
    }

    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception e) {
            // 忽略
        }
        return "system";
    }

    // ========== 多条件查询（关键词 + 学校 + 分类） ==========

    @Override
    public List<ArticleVO> getArticlesByConditions(String keyword, Integer schoolId, Integer categoryId) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        // 只查询已发布且在售的商品
        wrapper.eq("a.status", ArticleStatus.PUBLISHED);
        wrapper.eq("a.product_status", 0);

        // 发布时间不超过当前时间
        wrapper.le("a.published_at", LocalDateTime.now());

        // 关键词搜索（标题、内容）
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("a.title", keyword)
                    .or()
                    .like("a.content", keyword));
        }

        // 学校筛选
        if (schoolId != null && schoolId > 0) {
            wrapper.eq("a.school_id", schoolId);
        }

        // 分类筛选
        if (categoryId != null && categoryId > 0) {
            wrapper.eq("a.category_id", categoryId);
        }

        // 排序：置顶降序、发布时间降序
        wrapper.orderByDesc("a.is_top")
                .orderByDesc("a.published_at");

        Page<ArticleVO> page = new Page<>(1, 100);
        IPage<ArticleVO> result = baseMapper.selectArticleVOPage(page, wrapper);

        return result.getRecords();
    }

    // ========== 库存管理方法 ==========

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
            System.out.println("【库存扣减】库存不足 - articleId: " + articleId + ", 当前库存: " + article.getStock() + ", 需要: " + quantity);
            return false;
        }

        // 3. 扣减库存
        article.setStock(article.getStock() - quantity);
        System.out.println("【库存扣减】扣减成功 - articleId: " + articleId + ", 剩余库存: " + article.getStock());

        // 4. 库存为0时自动下架
        if (article.getStock() == 0) {
            article.setProductStatus(2);  // 2=已下架
            System.out.println("【库存扣减】库存为0，商品已自动下架 - articleId: " + articleId);
        }

        // 5. 更新数据库
        return this.updateById(article);
    }

    // ========== 按用户ID查询商品 ==========

    @Override
    public IPage<ArticleVO> getArticleVOPageByUserId(Integer page, Integer size, String keyword,
                                                     Integer statusFilter, LocalDateTime startTime,
                                                     LocalDateTime endTime, Integer userId) {
        Page<ArticleVO> pageParam = new Page<>(page, size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        // ========== 关键：按用户ID过滤 ==========
        wrapper.eq("user_id", userId);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }

        if (statusFilter != null) {
            wrapper.eq("status", statusFilter);
        }

        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }

        wrapper.orderByDesc("create_time");

        return baseMapper.selectArticleVOPage(pageParam, wrapper);
    }
}