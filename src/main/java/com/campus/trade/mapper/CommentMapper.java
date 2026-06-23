package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据文章ID获取所有评论（包含用户信息）
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comment c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} AND c.status = 1 " +
            "ORDER BY c.create_time ASC")
    List<Comment> selectCommentsByArticleId(@Param("articleId") Integer articleId);
}