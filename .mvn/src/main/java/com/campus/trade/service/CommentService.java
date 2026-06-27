package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> getAllComments();

    IPage<Comment> getCommentsByPage(Integer page, Integer size, String status, Integer articleId);

    Comment getCommentById(Integer id);

    List<Comment> getCommentsByArticleId(Integer articleId);

    boolean addComment(Comment comment);

    boolean updateComment(Comment comment);

    boolean deleteComment(Integer id, Integer userId);

    boolean deleteComments(List<Integer> ids);

    boolean approveComment(Integer id);

    boolean rejectComment(Integer id);
}