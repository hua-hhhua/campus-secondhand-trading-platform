package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.Order;
import com.campus.trade.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String orderManage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer statusFilter,
                              Model model) {
        IPage<Order> orderPage = orderService.page(
                new Page<>(pageNum, pageSize),
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .like(keyword != null && !keyword.isEmpty(), "order_no", keyword)
                        .or()
                        .like(keyword != null && !keyword.isEmpty(), "buyer_name", keyword)
                        .or()
                        .like(keyword != null && !keyword.isEmpty(), "seller_name", keyword)
                        .eq(statusFilter != null, "status", statusFilter)
                        .orderByDesc("created_at")
        );
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", statusFilter);
        return "admin/order-manage";
    }

    @GetMapping("/orders/{id}")
    @ResponseBody
    public Order getOrderDetail(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @PostMapping("/orders/status/{id}")
    @ResponseBody
    public boolean updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }

    @PostMapping("/orders/delete/{id}")
    @ResponseBody
    public boolean deleteOrder(@PathVariable Long id) {
        return orderService.adminDeleteOrder(id);
    }

    @PostMapping("/orders/delete-batch")
    @ResponseBody
    public boolean deleteOrders(@RequestBody List<Long> ids) {
        return orderService.removeByIds(ids);
    }
}
