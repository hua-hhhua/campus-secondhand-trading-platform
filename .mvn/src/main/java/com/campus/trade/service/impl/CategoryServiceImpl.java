package com.campus.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.Category;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.service.CategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public IPage<Category> getCategoriesByPage(Integer page, Integer size, String keyword) {
        Page<Category> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Category::getName, keyword).or().like(Category::getDescription, keyword);
        }
        wrapper.orderByAsc(Category::getSortOrder);
        return this.page(pageParam, wrapper);
    }

    @Override
    public List<Category> getCategoriesByParentId(Integer parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId).orderByAsc(Category::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public Category getCategoryById(Integer id) {
        return this.getById(id);
    }

    @Override
    public boolean addCategory(Category category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return this.save(category);
    }

    @Override
    public boolean updateCategory(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        return this.updateById(category);
    }

    @Override
    public boolean deleteCategory(Integer id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteCategories(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return this.removeByIds(ids);
    }
}