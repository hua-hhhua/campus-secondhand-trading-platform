package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 * 继承 BaseMapper 后，自动拥有单表的增删改查方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 如果需要自定义 SQL，可以在这里添加方法
     * 例如：根据用户名查询用户
     *
     * @Select("SELECT * FROM users WHERE username = #{username}")
     * User selectByUsername(String username);
     */
}