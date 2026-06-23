package com.campus.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Tag;
import com.campus.trade.mapper.TagMapper;
import com.campus.trade.service.TagService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> getAllTags() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Tag::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public IPage<Tag> getTagsByPage(Integer page, Integer size, String keyword) {
        Page<Tag> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Tag::getName, keyword).or().like(Tag::getSlug, keyword);
        }
        wrapper.orderByDesc(Tag::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public Tag getTagById(Integer id) {
        return this.getById(id);
    }

    @Override
    public boolean addTag(Tag tag) {
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        return this.save(tag);
    }

    @Override
    public boolean updateTag(Tag tag) {
        tag.setUpdateTime(LocalDateTime.now());
        return this.updateById(tag);
    }

    @Override
    public boolean deleteTag(Integer id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteTags(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return this.removeByIds(ids);
    }
}