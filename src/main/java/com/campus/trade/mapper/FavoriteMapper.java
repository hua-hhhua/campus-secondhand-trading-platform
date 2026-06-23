package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT COUNT(*) FROM favorite WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Integer articleId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND article_id = #{articleId}")
    int countByUserAndArticle(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
}