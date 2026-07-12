package com.campus.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.config.WebConfig;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 【用户模块-服务实现层】
 * 用户模块服务实现类，实现用户相关的业务逻辑，包括用户增删改查、登录注册、头像上传等功能
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 【用户模块-根据用户名加载用户】
     * 根据用户名加载用户信息，用于Spring Security认证，同时验证用户状态
     * @param username 用户名
     * @return Spring Security的UserDetails对象，包含用户名、密码和角色权限
     * @throws UsernameNotFoundException 用户不存在或状态异常时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户状态异常");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null && user.getRole() == 1 ? "ADMIN" : "USER")))
        );
    }

    /**
     * 【用户模块-根据用户名获取用户】
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    /**
     * 【用户模块-根据用户名查询用户】
     * 根据用户名查询用户（别名方法，内部调用getUserByUsername）
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    @Override
    public User findByUsername(String username) {
        return getUserByUsername(username);
    }

    /**
     * 【用户模块-搜索用户】
     * 分页搜索用户，支持关键词和时间范围筛选
     * @param page 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词，模糊匹配用户名、昵称、邮箱
     * @param startTime 创建时间起始值（大于等于）
     * @param endTime 创建时间结束值（小于等于）
     * @return 分页用户数据，按创建时间倒序排列
     */
    @Override
    public IPage<User> searchUsers(Integer page, Integer size, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        if (startTime != null) {
            wrapper.ge(User::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(User::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(User::getCreatedAt);
        return this.page(pageParam, wrapper);
    }

    /**
     * 【用户模块-管理员分页查询用户】
     * 分页查询用户（管理员专用），支持关键词、角色和状态筛选
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词，模糊匹配用户名、昵称、邮箱
     * @param role 用户角色筛选条件
     * @param status 用户状态筛选条件
     * @return 分页用户数据，按创建时间倒序排列
     */
    @Override
    public IPage<User> getUsersByPage(Integer pageNum, Integer pageSize, String keyword, Integer role, Integer status) {
        Page<User> pageParam = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreatedAt);
        return this.page(pageParam, wrapper);
    }

    /**
     * 【用户模块-添加用户】
     * 添加新用户，自动对密码进行BCrypt加密，并设置创建时间和更新时间
     * @param user 用户信息对象
     * @return 添加成功返回true，失败返回false
     */
    @Override
    public boolean addUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return this.save(user);
    }

    /**
     * 【用户模块-更新用户】
     * 更新用户信息，自动设置更新时间
     * @param user 用户信息对象
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return this.updateById(user);
    }

    /**
     * 【用户模块-删除用户】
     * 根据ID删除用户
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean deleteUser(Integer id) {
        return this.removeById(id);
    }

    /**
     * 【用户模块-批量删除用户】
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean deleteUsers(List<Integer> ids) {
        return this.removeByIds(ids);
    }

    /**
     * 【用户模块-更新用户状态】
     * 更新用户状态，自动设置更新时间
     * @param id 用户ID
     * @param status 用户状态值
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateUserStatus(Integer id, Integer status) {
        User user = this.getById(id);
        if (user != null) {
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            return this.updateById(user);
        }
        return false;
    }

    /**
     * 【用户模块-更新用户角色】
     * 更新用户角色，自动设置更新时间
     * @param id 用户ID
     * @param role 用户角色值
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateUserRole(Integer id, Integer role) {
        User user = this.getById(id);
        if (user != null) {
            user.setRole(role);
            user.setUpdatedAt(LocalDateTime.now());
            return this.updateById(user);
        }
        return false;
    }

    /**
     * 【用户模块-上传头像】
     * 上传用户头像文件，保存到本地并更新用户头像路径
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像访问路径（/avatars/文件名）
     * @throws RuntimeException 头像上传失败时抛出运行时异常
     */
    @Override
    public String uploadAvatar(MultipartFile file, Integer userId) {
        try {
            // 创建目录
            Path uploadDir = Paths.get(uploadPath, "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            Files.write(filePath, file.getBytes());

           // 返回访问路径（修改为 /avatars/ 以匹配 WebConfig 配置）
            String avatarPath = "/avatars/" + filename;

            // 更新用户头像
            User user = this.getById(userId);
            if (user != null) {
                user.setAvatar(avatarPath);
                user.setUpdatedAt(LocalDateTime.now());
                this.updateById(user);
            }

            return avatarPath;
        } catch (IOException e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 【用户模块-用户登录】
     * 用户登录验证，验证用户名和密码是否匹配
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * 【用户模块-用户注册】
     * 用户注册，检查用户名是否已存在，加密密码后保存，默认设置为普通用户、正常状态
     * @param user 用户注册信息对象
     * @return 注册成功返回true，失败返回false
     */
    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        User existing = getUserByUsername(user.getUsername());
        if (existing != null) {
            return false;
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(0);  // 普通用户
        user.setStatus(1); // 正常状态
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return this.save(user);
    }
}
