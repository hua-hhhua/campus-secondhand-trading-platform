package com.campus.trade.service;

import com.campus.trade.entity.ArticleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 */
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========== 缓存键名前缀 ==========
    public static final String PREFIX_ARTICLE_LIST = "article:list:";
    public static final String PREFIX_ARTICLE_DETAIL = "article:detail:";
    public static final String PREFIX_CATEGORY_LIST = "category:list";
    public static final String PREFIX_TAG_LIST = "tag:list";

    // ========== 缓存过期时间（秒） ==========
    private static final long ARTICLE_LIST_EXPIRE = 300; // 商品列表：5分钟
    private static final long ARTICLE_DETAIL_EXPIRE = 300; // 商品详情：5分钟
    private static final long CATEGORY_EXPIRE = 3600; // 分类列表：1小时
    private static final long TAG_EXPIRE = 3600; // 标签列表：1小时

    // ========== 商品列表缓存（硬编码ArticleVO类型，避免泛型擦除问题） ==========
    /**
     * 获取商品列表缓存
     */
    @SuppressWarnings("unchecked")
    public List<ArticleVO> getArticleList(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(PREFIX_ARTICLE_LIST + key);
            if (value != null) {
                System.out.println("【Redis缓存】获取到缓存数据，类型: " + value.getClass().getName());
                return (List<ArticleVO>) value;
            }
        } catch (Exception e) {
            System.out.println("【Redis缓存】获取商品列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置商品列表缓存
     */
    public void setArticleList(String key, List<ArticleVO> list) {
        try {
            redisTemplate.opsForValue().set(PREFIX_ARTICLE_LIST + key, list, ARTICLE_LIST_EXPIRE, TimeUnit.SECONDS);
            System.out.println("【Redis缓存】设置商品列表缓存: " + PREFIX_ARTICLE_LIST + key);
        } catch (Exception e) {
            System.out.println("【Redis缓存】设置商品列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 删除商品列表缓存
     */
    public void deleteArticleList(String key) {
        try {
            redisTemplate.delete(PREFIX_ARTICLE_LIST + key);
            System.out.println("【Redis缓存】删除商品列表缓存: " + PREFIX_ARTICLE_LIST + key);
        } catch (Exception e) {
            System.out.println("【Redis缓存】删除商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除所有商品列表缓存
     */
    public void deleteAllArticleList() {
        try {
            Set<String> keys = redisTemplate.keys(PREFIX_ARTICLE_LIST + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("【Redis缓存】删除所有商品列表缓存，数量: " + keys.size());
            }
        } catch (Exception e) {
            System.out.println("【Redis缓存】删除所有商品列表缓存失败: " + e.getMessage());
        }
    }

    // ========== 商品详情缓存 ==========
    /**
     * 获取商品详情缓存
     */
    public <T> T getArticleDetail(Integer articleId) {
        try {
            return (T) redisTemplate.opsForValue().get(PREFIX_ARTICLE_DETAIL + articleId);
        } catch (Exception e) {
            System.out.println("【Redis缓存】获取商品详情失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 设置商品详情缓存
     */
    public <T> void setArticleDetail(Integer articleId, T detail) {
        try {
            redisTemplate.opsForValue().set(PREFIX_ARTICLE_DETAIL + articleId, detail, ARTICLE_DETAIL_EXPIRE,
                    TimeUnit.SECONDS);
            System.out.println("【Redis缓存】设置商品详情缓存: " + PREFIX_ARTICLE_DETAIL + articleId);
        } catch (Exception e) {
            System.out.println("【Redis缓存】设置商品详情缓存失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品详情缓存
     */
    public void deleteArticleDetail(Integer articleId) {
        try {
            redisTemplate.delete(PREFIX_ARTICLE_DETAIL + articleId);
            System.out.println("【Redis缓存】删除商品详情缓存: " + PREFIX_ARTICLE_DETAIL + articleId);
        } catch (Exception e) {
            System.out.println("【Redis缓存】删除商品详情缓存失败: " + e.getMessage());
        }
    }

    /**
     * 删除所有商品详情缓存
     */
    public void deleteAllArticleDetail() {
        try {
            Set<String> keys = redisTemplate.keys(PREFIX_ARTICLE_DETAIL + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("【Redis缓存】删除所有商品详情缓存，数量: " + keys.size());
            }
        } catch (Exception e) {
            System.out.println("【Redis缓存】删除所有商品详情缓存失败: " + e.getMessage());
        }
    }

    // ========== 通用缓存操作 ==========
    /**
     * 设置缓存
     */
    public void set(String key, Object value, long expireSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("【Redis缓存】设置缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            System.out.println("【Redis缓存】获取缓存失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            System.out.println("【Redis缓存】删除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            System.out.println("【Redis缓存】判断key存在失败: " + e.getMessage());
        }
        return false;
    }
}
