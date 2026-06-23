package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Integer> selectTagIdsByArticleId(@Param("articleId") Integer articleId);
}