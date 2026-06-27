package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService extends IService<User>, UserDetailsService {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    User findByUsername(String username);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 分页搜索用户（支持关键词和时间范围）
     */
    IPage<User> searchUsers(Integer page, Integer size, String keyword, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询用户（管理员专用）
     */
    IPage<User> getUsersByPage(Integer pageNum, Integer pageSize, String keyword, Integer role, Integer status);

    /**
     * 添加用户
     */
    boolean addUser(User user);

    /**
     * 更新用户
     */
    boolean updateUser(User user);

    /**
     * 删除用户
     */
    boolean deleteUser(Integer id);

    /**
     * 批量删除用户
     */
    boolean deleteUsers(List<Integer> ids);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Integer id, Integer status);

    /**
     * 更新用户角色
     */
    boolean updateUserRole(Integer id, Integer role);

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file, Integer userId);

    /**
     * 登录
     */
    User login(String username, String password);

    /**
     * 注册
     */
    boolean register(User user);
}