package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.constant.ArticleStatus;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleResultMapVO;
import com.campus.trade.entity.ArticleVO;
import com.campus.trade.mapper.ArticleMapper;
import com.campus.trade.service.ArticleNotificationProducer;
import com.campus.trade.service.ArticleService;
import com.campus.trade.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleNotificationProducer articleNotificationProducer;

    @Autowired
    private AsyncService asyncService;

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
            article.setUpdateTime(LocalDateTime.now());
            this.updateById(article);
            System.out.println("【定时发布】文章已发布 - ID: " + article.getId() + ", 标题: " + article.getTitle());

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

        if (article.getId() == null) {
            return doCreateArticle(article);
        } else {
            return doUpdateArticle(article);
        }
    }

    private boolean doCreateArticle(Article article) {
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        handlePublishedAt(article);

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
        handlePublishedAt(article);

        boolean result = this.updateById(article);

        if (result) {
            System.out.println("【文章保存】文章已更新 - ID: " + article.getId() + ", 标题: " + article.getTitle());
            asyncService.asyncSendArticleNotification(article, "update");
        }

        return result;
    }

    private void handlePublishedAt(Article article) {
        if (article.getStatus() == ArticleStatus.SCHEDULED) {
            if (article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now().plusHours(1));
                System.out.println("【发布时间处理】定时发布未设置时间，已自动设置为1小时后");
            }
        } else if (article.getStatus() == ArticleStatus.DRAFT && article.getPublishedAt() != null) {
            article.setStatus(ArticleStatus.SCHEDULED);
            System.out.println("【发布时间处理】草稿含有发布时间，已自动转为定时发布");
        } else if (article.getStatus() == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
    }

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

        // 只查询已发布的文章 - 加表别名 a.
        wrapper.eq("a.status", ArticleStatus.PUBLISHED);

        // 发布时间不超过当前时间 - 加表别名 a.
        wrapper.le("a.published_at", LocalDateTime.now());

        // 关键字搜索 - 加表别名 a.
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("a.title", keyword)
                    .or()
                    .like("a.content", keyword)
                    .or()
                    .like("a.summary", keyword));
        }

        // 排序：置顶降序、发布时间降序 - 加表别名 a.
        wrapper.orderByDesc("a.is_top")
                .orderByDesc("a.published_at");

        // 使用JOIN查询，直接返回ArticleVO列表
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
}