package com.campus.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

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

    @Override
    public boolean addUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return this.save(user);
    }

    @Override
    public boolean updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Integer id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteUsers(List<Integer> ids) {
        return this.removeByIds(ids);
    }

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

            // 返回访问路径
            String avatarPath = "/uploads/avatars/" + filename;

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

    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

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