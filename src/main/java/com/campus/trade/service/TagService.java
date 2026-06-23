package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    List<Tag> getAllTags();

    IPage<Tag> getTagsByPage(Integer page, Integer size, String keyword);

    Tag getTagById(Integer id);

    boolean addTag(Tag tag);

    boolean updateTag(Tag tag);

    boolean deleteTag(Integer id);

    boolean deleteTags(List<Integer> ids);
}