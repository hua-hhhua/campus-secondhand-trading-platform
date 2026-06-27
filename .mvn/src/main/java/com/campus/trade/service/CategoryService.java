package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 获取所有分类
     */
    List<Category> getAllCategories();

    /**
     * 分页查询分类
     */
    IPage<Category> getCategoriesByPage(Integer page, Integer size, String keyword);

    /**
     * 根据父ID获取子分类
     */
    List<Category> getCategoriesByParentId(Integer parentId);

    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Integer id);

    /**
     * 添加分类
     */
    boolean addCategory(Category category);

    /**
     * 更新分类
     */
    boolean updateCategory(Category category);

    /**
     * 删除分类
     */
    boolean deleteCategory(Integer id);

    /**
     * 批量删除分类
     */
    boolean deleteCategories(List<Integer> ids);
}