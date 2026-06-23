package com.campus.trade.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MyBatisPlus 拦截器链到 Spring 容器
     * @return MyBatis 插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MyBatisPlus 拦截器链
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加自动分页拦截器并指定数据库类型
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}

//用户管理页面
//http://localhost:8080/admin/users
//单个用户查询页面
//http://localhost:8080/admin/users/edit/4
//清空缓存
//cd /d "D:\viours apps\Redis-x64-5.0.14.1"
//        redis-cli.exe
//        FLUSHALL
//        KEYS *
//        EXIT