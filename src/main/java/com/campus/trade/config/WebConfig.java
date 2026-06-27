package com.campus.trade.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Web MVC 配置类 - 用于映射静态资源路径（用户头像 + 商品图片）
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
     * 添加资源处理器
     * 访问示例：http://localhost:8080/avatars/xxx.jpg
     *          http://localhost:8080/uploads/articles/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用户头像路径映射
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadPath + "/avatars/");

        // 商品图片路径映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
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
                        "/avatars/**",
                        "/uploads/**"
                );

        // 错误日志拦截器
        registry.addInterceptor(errorLoggingInterceptor)
                .addPathPatterns("/**");
    }
}
@RestController
class AvatarController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path avatarPath = Paths.get(uploadPath, "avatars", filename);

            if (!Files.exists(avatarPath)) {
                // 如果头像不存在，返回默认头像
                Path defaultAvatar = Paths.get(uploadPath, "avatars", "default-avatar.png");

                // 如果默认头像也不存在，创建一个简单的占位符
                if (!Files.exists(defaultAvatar)) {
                    // 创建默认头像目录
                    Files.createDirectories(Paths.get(uploadPath, "avatars"));
                    // 这里可以返回一个简单的文本占位符或生成默认图片
                    return ResponseEntity.notFound().build();
                }

                avatarPath = defaultAvatar;
            }

            Resource resource = new UrlResource(avatarPath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}