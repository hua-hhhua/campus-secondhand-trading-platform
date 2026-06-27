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
import java.math.BigDecimal;

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
     * 用户列表页（普通用户查看）
     */
    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.list());
        return "users";
    }

    /**
     * 上传头像
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }
        return result;
    }

    // ========== API接口 ==========

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
    public Page<User> getPage(@RequestParam(defaultValue = "1") Integer current,
                              @RequestParam(defaultValue = "10") Integer size) {
        return userService.page(new Page<>(current, size));
    }

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

    @PostMapping("/api/users")
    @ResponseBody
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public boolean update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        return userService.updateById(user);
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public boolean delete(@PathVariable Integer id) {
        return userService.removeById(id);
    }

    // ========== API 登录接口（返回 JSON，支持 Session） ==========

    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> apiLogin(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // 使用 Spring Security 的 AuthenticationManager 进行认证
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(token);

            // 手动设置 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将 SecurityContext 保存到 Session
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

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
    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ========== 我的订单 ==========

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
        if (order == null || !order.getBuyerId().equals(user.getId())) {
            return "redirect:/user/my-orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("userNickname", user.getNickname());
        model.addAttribute("userAvatar", user.getAvatar());
        return "user/order-detail";
    }

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

    // ========== 支付订单 ==========
    @PostMapping("/orders/{id}/pay")
    @ResponseBody
    public Result payOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        try {
            boolean success = orderService.payOrder(id, user.getId());
            return success ? Result.success("支付成功") : Result.error("支付失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 获取待支付总额（用于前端显示） ==========
    @GetMapping("/orders/pending-amount")
    @ResponseBody
    public Result getPendingAmount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("请先登录");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        BigDecimal amount = orderService.calculatePendingAmount(user.getId());
        return Result.success(String.valueOf(amount));
    }
}