package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【用户模块-服务接口层】
 * 用户模块服务接口，定义用户相关的业务逻辑方法，包括用户增删改查、登录注册、头像上传等功能
 */
public interface UserService extends IService<User>, UserDetailsService {

    /**
     * 【用户模块-根据用户名查询用户】
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    User findByUsername(String username);

    /**
     * 【用户模块-根据用户名获取用户】
     * 根据用户名获取用户信息（与findByUsername功能相同，方法名不同）
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    User getUserByUsername(String username);

    /**
     * 【用户模块-搜索用户】
     * 分页搜索用户，支持关键词和时间范围筛选
     * @param page 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词，匹配用户名、昵称、邮箱
     * @param startTime 创建时间起始值
     * @param endTime 创建时间结束值
     * @return 分页用户数据
     */
    IPage<User> searchUsers(Integer page, Integer size, String keyword, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 【用户模块-管理员分页查询用户】
     * 分页查询用户（管理员专用），支持关键词、角色和状态筛选
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词，匹配用户名、昵称、邮箱
     * @param role 用户角色筛选
     * @param status 用户状态筛选
     * @return 分页用户数据
     */
    IPage<User> getUsersByPage(Integer pageNum, Integer pageSize, String keyword, Integer role, Integer status);

    /**
     * 【用户模块-添加用户】
     * 添加新用户，自动对密码进行加密处理
     * @param user 用户信息对象
     * @return 添加成功返回true，失败返回false
     */
    boolean addUser(User user);

    /**
     * 【用户模块-更新用户】
     * 更新用户信息
     * @param user 用户信息对象
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUser(User user);

    /**
     * 【用户模块-删除用户】
     * 根据ID删除用户
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteUser(Integer id);

    /**
     * 【用户模块-批量删除用户】
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteUsers(List<Integer> ids);

    /**
     * 【用户模块-更新用户状态】
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态值
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUserStatus(Integer id, Integer status);

    /**
     * 【用户模块-更新用户角色】
     * 更新用户角色
     * @param id 用户ID
     * @param role 用户角色值
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUserRole(Integer id, Integer role);

    /**
     * 【用户模块-上传头像】
     * 上传用户头像文件并更新用户头像路径
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像访问路径
     */
    String uploadAvatar(MultipartFile file, Integer userId);

    /**
     * 【用户模块-用户登录】
     * 用户登录验证，验证用户名和密码是否匹配
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    User login(String username, String password);

    /**
     * 【用户模块-用户注册】
     * 用户注册，检查用户名是否已存在，加密密码后保存
     * @param user 用户注册信息对象
     * @return 注册成功返回true，失败返回false
     */
    boolean register(User user);
}
