package com.campus.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.entity.School;
import com.campus.trade.mapper.SchoolMapper;
import com.campus.trade.service.SchoolService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {

    @Override
    public List<School> getAllSchools() {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(School::getName);
        return this.list(wrapper);
    }

    @Override
    public IPage<School> getSchoolsByPage(Integer page, Integer size, String keyword) {
        Page<School> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(School::getName, keyword).or().like(School::getCity, keyword);
        }
        wrapper.orderByAsc(School::getName);
        return this.page(pageParam, wrapper);
    }

    @Override
    public School getSchoolById(Integer id) {
        return this.getById(id);
    }

    @Override
    public boolean addSchool(School school) {
        school.setCreateTime(LocalDateTime.now());
        school.setUpdateTime(LocalDateTime.now());
        return this.save(school);
    }

    @Override
    public boolean updateSchool(School school) {
        school.setUpdateTime(LocalDateTime.now());
        return this.updateById(school);
    }

    @Override
    public boolean deleteSchool(Integer id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteSchools(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return this.removeByIds(ids);
    }
}
