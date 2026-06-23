package com.campus.trade.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类 - 用于映射静态资源路径（用户头像）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 文件上传根目录，从配置文件读取
     */
    @Value("${file.upload.path}")
    private String uploadPath;

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Autowired
    private ErrorLoggingInterceptor errorLoggingInterceptor;

    /**
     * 添加资源处理器，将 /avatars/** 映射到 D:/campus_trade_files/avatars/
     *
     * 访问示例：http://localhost:8080/avatars/xxx.jpg
     * 实际路径：D:/campus_trade_files/avatars/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用户头像路径映射
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadPath + "/avatars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户信息拦截器
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/toLoginPage",
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/static/**",
                        "/avatars/**"
                );

        // 错误日志拦截器
        registry.addInterceptor(errorLoggingInterceptor)
                .addPathPatterns("/**");
    }
}