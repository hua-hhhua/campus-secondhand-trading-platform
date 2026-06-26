package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.Result;
import com.campus.trade.entity.User;
import com.campus.trade.service.AsyncService;
import com.campus.trade.service.OrderService;
import com.campus.trade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import com.campus.trade.dto.UserUpdateDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private OrderService orderService;

    /**
     * 个人信息页面
     */
    @GetMapping("/user/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "user/profile";
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/user/update")
    @ResponseBody
    public Result updateProfile(@RequestBody UserUpdateDTO dto, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新昵称
        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.setNickname(dto.getNickname());
        }

        // 更新手机号
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            user.setPhone(dto.getPhone());
        }

        // 修改密码（需要验证旧密码）
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            // 验证旧密码
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
     * 用户列表页（普通页面）
     */
    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.list());
        return "users";
    }

    // ========== 注意：/admin/users 的管理功能已在 AdminController 中实现，这里不再重复 ==========

    @GetMapping("/admin/users/add")
    public String addUserPage(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }

    @GetMapping("/admin/users/edit/{id}")
    public String editUserPage(@PathVariable Integer id, Model model) {
        System.out.println("请求参数：第一次查询从数据库获取并保存，第二次从缓存中获取");
        User user = userService.getById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(User user) {
        String operationType = "";
        String username = user.getUsername();

        if (user.getId() == null) {
            user.setRole(0);
            user.setStatus(1);
            userService.save(user);
            operationType = "新增用户";

            asyncService.asyncLogOperation("admin", operationType, "用户名: " + username);
            asyncService.asyncSendNotification(username, "欢迎注册", "感谢您注册校园交易平台");
        } else {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                User existing = userService.getById(user.getId());
                if (existing != null) {
                    user.setPassword(existing.getPassword());
                }
            }
            userService.updateById(user);
            operationType = "编辑用户";

            asyncService.asyncLogOperation("admin", operationType, "用户ID: " + user.getId() + ", 用户名: " + user.getUsername());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        User user = userService.getById(id);
        String username = user != null ? user.getUsername() : String.valueOf(id);
        userService.removeById(id);

        asyncService.asyncLogOperation("admin", "删除用户", "用户ID: " + id + ", 用户名: " + username);

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/batchDelete")
    public String batchDelete(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            userService.removeById(Integer.parseInt(id));
        }

        asyncService.asyncLogOperation("admin", "批量删除用户", "删除用户ID列表: " + ids);

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/upload-avatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userId") Integer userId) {
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
            String avatarPath = userService.uploadAvatar(file, userId);
            result.put("success", true);
            result.put("avatarPath", avatarPath);
            result.put("message", "上传成功");

            asyncService.asyncLogOperation("user_" + userId, "上传头像", "头像路径: " + avatarPath);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<User> listAll() {
        return userService.list();
    }

    @GetMapping("/api/users/{id}")
    @ResponseBody
    public User getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    @GetMapping("/api/users/username/{username}")
    @ResponseBody
    public User getByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/api/users/page")
    @ResponseBody
    public Page<User> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> page = new Page<>(current, size);
        return userService.page(page);
    }

    @PostMapping("/api/users/login")
    @ResponseBody
    public User login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (user != null) {
            asyncService.asyncLogOperation(loginRequest.getUsername(), "用户登录", "登录成功");
        } else {
            asyncService.asyncLogOperation(loginRequest.getUsername(), "用户登录", "登录失败 - 用户名或密码错误");
        }

        return user;
    }

    @PostMapping("/api/users/register")
    @ResponseBody
    public boolean register(@RequestBody User user) {
        boolean result = userService.register(user);

        if (result) {
            asyncService.asyncLogOperation(user.getUsername(), "用户注册", "注册成功");
            asyncService.asyncSendNotification(user.getUsername(), "欢迎注册", "感谢您注册校园交易平台");
        } else {
            asyncService.asyncLogOperation(user.getUsername(), "用户注册", "注册失败");
        }

        return result;
    }

    @PostMapping("/api/users")
    @ResponseBody
    public boolean save(@RequestBody User user) {
        boolean result = userService.save(user);

        if (result) {
            asyncService.asyncLogOperation("api", "新增用户", "用户名: " + user.getUsername());
        }

        return result;
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public boolean update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        boolean result = userService.updateById(user);

        if (result) {
            asyncService.asyncLogOperation("api", "更新用户", "用户ID: " + id);
        }

        return result;
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public boolean delete(@PathVariable Integer id) {
        User user = userService.getById(id);
        String username = user != null ? user.getUsername() : String.valueOf(id);
        boolean result = userService.removeById(id);

        if (result) {
            asyncService.asyncLogOperation("api", "删除用户", "用户ID: " + id + ", 用户名: " + username);
        }

        return result;
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ========== 订单管理 ==========

    /**
     * 我的订单列表页（买家）
     */
    @GetMapping("/user/my-orders")
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

        // 获取买家的订单列表
        List<Order> allOrders = orderService.getBuyerOrders(user.getId());

        // 按状态筛选
        if (statusFilter != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getStatus().equals(statusFilter))
                    .collect(java.util.stream.Collectors.toList());
        }

        // 手动分页
        int total = allOrders.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<Order> orders = allOrders.subList(Math.max(0, start), Math.min(end, total));
        
        // 创建分页对象
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
     * 取消订单
     */
    @PostMapping("/user/orders/{id}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable Long id,
                              @RequestParam(required = false) String reason,
                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }

        try {
            boolean success = orderService.cancelOrder(id, reason);
            if (success) {
                asyncService.asyncLogOperation(authentication.getName(), "取消订单", "订单ID: " + id);
                return Result.success("订单已取消");
            } else {
                return Result.error("取消失败，订单可能不存在或状态不允许取消");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/user/orders/{id}/confirm")
    @ResponseBody
    public Result confirmReceipt(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }

        try {
            boolean success = orderService.confirmReceipt(id);
            if (success) {
                asyncService.asyncLogOperation(authentication.getName(), "确认收货", "订单ID: " + id);
                return Result.success("确认收货成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除订单
     */
    @PostMapping("/user/orders/{id}/delete")
    @ResponseBody
    public Result deleteOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }

        try {
            boolean success = orderService.removeById(id);
            if (success) {
                asyncService.asyncLogOperation(authentication.getName(), "删除订单", "订单ID: " + id);
                return Result.success("订单已删除");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 订单详情页
     */
    @GetMapping("/user/orders/{id}")
    public String orderDetail(@PathVariable Long id,
                              Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login";
        }

        // 获取订单详情
        Order order = orderService.getById(id);
        if (order == null) {
            return "redirect:/user/my-orders";
        }

        // 验证权限(只能查看自己的订单)
        if (!order.getBuyerId().equals(user.getId())) {
            return "redirect:/user/my-orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userNickname", user.getNickname());
        model.addAttribute("userAvatar", user.getAvatar());
        
        return "user/order-detail";
    }

    /**
     * 我的销售订单列表页（卖家）
     */
    @GetMapping("/user/seller-orders")
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

        // 获取卖家的销售订单列表
        List<Order> allOrders = orderService.getSellerOrders(user.getId());

        // 按状态筛选
        if (statusFilter != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getStatus().equals(statusFilter))
                    .collect(java.util.stream.Collectors.toList());
        }

        // 手动分页
        int total = allOrders.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<Order> orders = allOrders.subList(Math.max(0, start), Math.min(end, total));
        
        // 创建分页对象
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
     * 发货
     */
    @PostMapping("/user/orders/{id}/ship")
    @ResponseBody
    public Result shipOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }

        try {
            boolean success = orderService.shipOrder(id);
            if (success) {
                asyncService.asyncLogOperation(authentication.getName(), "发货", "订单ID: " + id);
                return Result.success("发货成功");
            } else {
                return Result.error("发货失败，订单可能不存在或状态不允许发货");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}