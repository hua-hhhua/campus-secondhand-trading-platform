package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.Result;
import com.campus.trade.entity.User;
import com.campus.trade.dto.UserUpdateDTO;
import com.campus.trade.service.AsyncService;
import com.campus.trade.service.OrderService;
import com.campus.trade.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【用户模块-控制层】
 * 用户模块控制器，处理用户相关的HTTP请求，包括个人信息管理、用户列表、登录注册、订单管理等功能
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ========== 个人信息 ==========

    /**
     * 【用户模块-查看个人信息】
     * 显示当前登录用户的个人信息页面
     * 
     * @param model          视图模型对象，用于传递数据到前端页面
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 个人信息页面视图名称，未登录则重定向到登录页
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        // 检查是否已登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/toLoginPage";
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/toLoginPage";
        }
        model.addAttribute("user", user);
        return "user/profile";
    }

    /**
     * 【用户模块-更新个人信息】
     * 更新当前登录用户的个人信息，包括昵称、手机号和密码
     * 
     * @param dto            用户更新信息数据传输对象，包含昵称、手机号、原密码、新密码、确认密码
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 操作结果，成功或失败信息
     */
    @PostMapping("/update")
    @ResponseBody
    public Result updateProfile(@RequestBody UserUpdateDTO dto, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.setNickname(dto.getNickname());
        }

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(dto.getPassword(), user.getPassword())) {
                return Result.error("原密码错误");
            }
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                return Result.error("两次密码输入不一致");
            }
            user.setPassword(encoder.encode(dto.getNewPassword()));
        }

        userService.updateById(user);
        return Result.success("个人信息更新成功");
    }

    /**
     * 【用户模块-用户列表页】
     * 用户列表页（普通用户查看），显示所有用户列表
     * 
     * @param model 视图模型对象，用于传递用户列表数据到前端页面
     * @return 用户列表页面视图名称
     */
    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.list());
        return "users";
    }

    /**
     * 【用户模块-上传头像】
     * 上传用户头像文件，支持图片格式校验和大小限制
     * 
     * @param file           上传的头像文件
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 上传结果Map，包含成功状态、消息和头像路径
     */
    @PostMapping("/uploadAvatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要上传的图片");
                return result;
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "只能上传图片文件");
                return result;
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "图片大小不能超过 5MB");
                return result;
            }

            // 从 Authentication 获取当前用户ID
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Integer userId = user.getId();

            String avatarPath = userService.uploadAvatar(file, userId);
            result.put("success", true);
            result.put("data", avatarPath);
            result.put("message", "上传成功");
            asyncService.asyncLogOperation("user_" + userId, "上传头像", "头像路径: " + avatarPath);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }
        return result;
    }

    // ========== API接口 ==========

    /**
     * 【用户模块-查询所有用户】
     * 获取所有用户列表的API接口
     * 
     * @return 用户列表
     */
    @GetMapping("/api/users")
    @ResponseBody
    public List<User> listAll() {
        return userService.list();
    }

    /**
     * 【用户模块-根据ID查询用户】
     * 根据用户ID查询用户信息的API接口
     * 
     * @param id 用户ID
     * @return 用户对象
     */
    @GetMapping("/api/users/{id}")
    @ResponseBody
    public User getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    /**
     * 【用户模块-根据用户名查询用户】
     * 根据用户名查询用户信息的API接口
     * 
     * @param username 用户名
     * @return 用户对象
     */
    @GetMapping("/api/users/username/{username}")
    @ResponseBody
    public User getByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * 【用户模块-分页查询用户】
     * 分页查询用户列表的API接口
     * 
     * @param current 当前页码，默认为1
     * @param size    每页大小，默认为10
     * @return 分页用户数据
     */
    @GetMapping("/api/users/page")
    @ResponseBody
    public Page<User> getPage(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return userService.page(new Page<>(current, size));
    }

    /**
     * 【用户模块-用户登录】
     * 用户登录验证的API接口
     * 
     * @param loginRequest 登录请求对象，包含用户名和密码
     * @return 登录成功返回用户对象，失败返回null
     */
    @PostMapping("/api/users/login")
    @ResponseBody
    public User login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            asyncService.asyncLogOperation(loginRequest.getUsername(), "用户登录", "登录成功");
        } else {
            asyncService.asyncLogOperation(loginRequest.getUsername(), "用户登录", "登录失败");
        }
        return user;
    }

    /**
     * 【用户模块-用户注册】
     * 用户注册的API接口
     * 
     * @param user 用户注册信息对象
     * @return 注册成功返回true，失败返回false
     */
    @PostMapping("/api/users/register")
    @ResponseBody
    public boolean register(@RequestBody User user) {
        boolean result = userService.register(user);
        if (result) {
            asyncService.asyncLogOperation(user.getUsername(), "用户注册", "注册成功");
            asyncService.asyncSendNotification(user.getUsername(), "欢迎注册", "感谢您注册校园交易平台");
        }
        return result;
    }

    /**
     * 【用户模块-新增用户】
     * 新增用户的API接口
     * 
     * @param user 用户信息对象
     * @return 新增成功返回true，失败返回false
     */
    @PostMapping("/api/users")
    @ResponseBody
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 【用户模块-更新用户】
     * 根据ID更新用户信息的API接口
     * 
     * @param id   用户ID
     * @param user 更新后的用户信息对象
     * @return 更新成功返回true，失败返回false
     */
    @PutMapping("/api/users/{id}")
    @ResponseBody
    public boolean update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        return userService.updateById(user);
    }

    /**
     * 【用户模块-删除用户】
     * 根据ID删除用户的API接口
     * 
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public boolean delete(@PathVariable Integer id) {
        return userService.removeById(id);
    }

    // ========== API 登录接口（返回 JSON，支持 Session） ==========

    /**
     * 【用户模块-API登录】
     * API登录接口，使用Spring Security进行认证并支持Session，返回JSON格式结果
     * 
     * @param loginRequest 登录请求Map，包含username和password
     * @param session      HttpSession对象，用于存储认证信息
     * @return 登录结果Map，包含成功状态、消息和用户名
     */
    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> apiLogin(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // 使用 Spring Security 的 AuthenticationManager 进行认证
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(token);

            // 手动设置 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将 SecurityContext 保存到 Session
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("username", username);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录失败：" + e.getMessage());
            return result;
        }
    }

    // ========== 内部类 ==========

    /**
     * 【用户模块-登录请求对象】
     * 登录请求内部类，封装用户名和密码
     */
    public static class LoginRequest {
        private String username;
        private String password;

        /**
         * 【用户模块-获取用户名】
         * 获取登录请求中的用户名
         * 
         * @return 用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 【用户模块-设置用户名】
         * 设置登录请求中的用户名
         * 
         * @param username 用户名
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * 【用户模块-获取密码】
         * 获取登录请求中的密码
         * 
         * @return 密码
         */
        public String getPassword() {
            return password;
        }

        /**
         * 【用户模块-设置密码】
         * 设置登录请求中的密码
         * 
         * @param password 密码
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    // ========== 我的订单 ==========

    /**
     * 【用户模块-我的订单列表】
     * 显示当前登录用户作为买家的订单列表，支持分页和状态筛选
     * 
     * @param pageNum        当前页码，默认为1
     * @param pageSize       每页大小，默认为10
     * @param statusFilter   订单状态筛选条件，可选
     * @param model          视图模型对象，用于传递订单数据到前端页面
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 我的订单页面视图名称，未登录则重定向到登录页
     */
    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer statusFilter,
            Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> allOrders = orderService.getBuyerOrders(user.getId());
        if (statusFilter != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getStatus().equals(statusFilter))
                    .collect(java.util.stream.Collectors.toList());
        }

        int total = allOrders.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Order> orders = allOrders.subList(Math.max(0, start), Math.min(end, total));

        IPage<Order> orderPage = new Page<>(pageNum, pageSize, total);
        orderPage.setRecords(orders);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userNickname", user.getNickname());
        model.addAttribute("userAvatar", user.getAvatar());
        return "user/my-orders";
    }

    /**
     * 【用户模块-取消订单】
     * 买家取消订单的接口
     * 
     * @param id             订单ID
     * @param reason         取消原因，可选
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 操作结果，成功或失败信息
     */
    @PostMapping("/orders/{id}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable Long id, @RequestParam(required = false) String reason,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }
        try {
            boolean success = orderService.cancelOrder(id, reason);
            return success ? Result.success("订单已取消") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【用户模块-确认收货】
     * 买家确认收货的接口
     * 
     * @param id             订单ID
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 操作结果，成功或失败信息
     */
    @PostMapping("/orders/{id}/confirm")
    @ResponseBody
    public Result confirmReceipt(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }
        try {
            boolean success = orderService.confirmReceipt(id);
            return success ? Result.success("确认收货成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【用户模块-删除订单】
     * 删除订单的接口
     * 
     * @param id             订单ID
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 操作结果，成功或失败信息
     */
    @PostMapping("/orders/{id}/delete")
    @ResponseBody
    public Result deleteOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }
        try {
            boolean success = orderService.removeById(id);
            return success ? Result.success("订单已删除") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【用户模块-订单详情】
     * 显示订单详情页面
     * 
     * @param id             订单ID
     * @param model          视图模型对象，用于传递订单数据到前端页面
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 订单详情页面视图名称，未登录或不是本人订单则重定向
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderService.getById(id);
        if (order == null || (!order.getBuyerId().equals(user.getId()) && !order.getSellerId().equals(user.getId()))) {
            return "redirect:/user/my-orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userNickname", user.getNickname());
        model.addAttribute("userAvatar", user.getAvatar());
        return "user/order-detail";
    }

    /**
     * 【用户模块-卖家订单列表】
     * 显示当前登录用户作为卖家的订单列表，支持分页和状态筛选
     * 
     * @param pageNum        当前页码，默认为1
     * @param pageSize       每页大小，默认为10
     * @param statusFilter   订单状态筛选条件，可选
     * @param model          视图模型对象，用于传递订单数据到前端页面
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 卖家订单页面视图名称，未登录则重定向到登录页
     */
    @GetMapping("/seller-orders")
    public String sellerOrders(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer statusFilter,
            Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> allOrders = orderService.getSellerOrders(user.getId());
        if (statusFilter != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getStatus().equals(statusFilter))
                    .collect(java.util.stream.Collectors.toList());
        }

        int total = allOrders.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Order> orders = allOrders.subList(Math.max(0, start), Math.min(end, total));

        IPage<Order> orderPage = new Page<>(pageNum, pageSize, total);
        orderPage.setRecords(orders);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userNickname", user.getNickname());
        model.addAttribute("userAvatar", user.getAvatar());
        return "user/seller-orders";
    }

    /**
     * 【用户模块-订单发货】
     * 卖家对订单进行发货操作的接口
     * 
     * @param id             订单ID
     * @param authentication Spring Security认证对象，包含当前登录用户信息
     * @return 操作结果，成功或失败信息
     */
    @PostMapping("/orders/{id}/ship")
    @ResponseBody
    public Result shipOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }
        try {
            boolean success = orderService.shipOrder(id);
            return success ? Result.success("发货成功") : Result.error("发货失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
