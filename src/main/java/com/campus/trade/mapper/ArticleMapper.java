package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.trade.entity.Article;
import com.campus.trade.entity.ArticleResultMapVO;
import com.campus.trade.entity.ArticleVO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;  // ⬅️ 添加这个导入

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 多表查询文章列表（包含作者和分类信息）- 方案二：JOIN查询
     */
    @Select("SELECT a.*, " +
            "u.nickname as author_name, " +
            "u.avatar as author_avatar, " +
            "c.name as category_name, " +
            "c.slug as category_slug " +
            "FROM article a " +
            "LEFT JOIN users u ON a.user_id = u.id " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "${ew.customSqlSegment}")
    IPage<ArticleVO> selectArticleVOPage(IPage<ArticleVO> page, @Param(Constants.WRAPPER) Wrapper<Article> wrapper);

    // ========== 方案三：MyBatis ResultMap 关联查询（支持懒加载） ==========

    /**
     * 使用ResultMap关联查询文章列表（包含author和category对象）
     * 支持懒加载，只有在访问author或category时才执行对应的查询
     *
     * @param page 分页对象
     * @param wrapper 条件构造器
     * @return 分页的文章扩展对象（包含author和category对象）
     */
    @Select("SELECT * FROM article ${ew.customSqlSegment}")
    @Results(id = "ArticleResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "slug", property = "slug"),
            @Result(column = "content", property = "content"),
            @Result(column = "summary", property = "summary"),
            @Result(column = "cover_image", property = "coverImage"),
            @Result(column = "status", property = "status"),
            @Result(column = "view_count", property = "viewCount"),
            @Result(column = "is_top", property = "isTop"),
            @Result(column = "allow_comment", property = "allowComment"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "send_email", property = "sendEmail"),
            // 关联查询作者（支持懒加载 - LAZY）
            @Result(property = "author", column = "user_id",
                    one = @One(select = "com.campus.trade.mapper.UserMapper.selectById",
                            fetchType = FetchType.LAZY)),
            // 关联查询分类（支持懒加载 - LAZY）
            @Result(property = "category", column = "category_id",
                    one = @One(select = "com.campus.trade.mapper.CategoryMapper.selectById",
                            fetchType = FetchType.LAZY))
    })
    IPage<ArticleResultMapVO> selectArticleResultMapVOs(IPage<Article> page,
                                                        @Param(Constants.WRAPPER) Wrapper<Article> wrapper);
}