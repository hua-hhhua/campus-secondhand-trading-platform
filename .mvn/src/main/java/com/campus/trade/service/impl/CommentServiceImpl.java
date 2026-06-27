package com.campus.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Comment;
import com.campus.trade.mapper.CommentMapper;
import com.campus.trade.service.CommentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    public List<Comment> getAllComments() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public IPage<Comment> getCommentsByPage(Integer page, Integer size, String status, Integer articleId) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Comment::getStatus, Integer.parseInt(status));
        }

        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        wrapper.orderByDesc(Comment::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public Comment getCommentById(Integer id) {
        return this.getById(id);
    }

    @Override
    public List<Comment> getCommentsByArticleId(Integer articleId) {
        return baseMapper.selectCommentsByArticleId(articleId);
    }

    @Override
    public boolean addComment(Comment comment) {
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        if (comment.getParentId() == null) {
            comment.setParentId(0);
        }
        return this.save(comment);
    }

    @Override
    public boolean updateComment(Comment comment) {
        comment.setUpdateTime(LocalDateTime.now());
        return this.updateById(comment);
    }

    @Override
    public boolean deleteComment(Integer id, Integer userId) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteComments(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return this.removeByIds(ids);
    }

    @Override
    public boolean approveComment(Integer id) {
        Comment comment = this.getById(id);
        if (comment != null) {
            comment.setStatus(1);
            comment.setUpdateTime(LocalDateTime.now());
            return this.updateById(comment);
        }
        return false;
    }

    @Override
    public boolean rejectComment(Integer id) {
        Comment comment = this.getById(id);
        if (comment != null) {
            comment.setStatus(0);
            comment.setUpdateTime(LocalDateTime.now());
            return this.updateById(comment);
        }
        return false;
    }
}