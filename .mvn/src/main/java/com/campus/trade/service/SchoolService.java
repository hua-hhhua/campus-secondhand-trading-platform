package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.School;

import java.util.List;

public interface SchoolService extends IService<School> {

    /**
     * 获取所有学校
     */
    List<School> getAllSchools();

    /**
     * 分页查询学校
     */
    IPage<School> getSchoolsByPage(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取学校
     */
    School getSchoolById(Integer id);

    /**
     * 添加学校
     */
    boolean addSchool(School school);

    /**
     * 更新学校
     */
    boolean updateSchool(School school);

    /**
     * 删除学校
     */
    boolean deleteSchool(Integer id);

    /**
     * 批量删除学校
     */
    boolean deleteSchools(List<Integer> ids);
}
