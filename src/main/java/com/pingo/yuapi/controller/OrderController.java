package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.dto.CreateOrderRequest;
import com.pingo.yuapi.dto.OrderPaymentResponse;
import com.pingo.yuapi.entity.Order;
import com.pingo.yuapi.service.OrderService;
import com.pingo.yuapi.service.UserService;
import com.pingo.yuapi.utils.WechatPayUtils;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Resource
    private UserService userService;

    /**
     * 获取我的订单列表（最近一周）
     */
    @GetMapping("/my")
    public Result<List<Order>> getMyOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 假设从当前会话或token中获取用户ID
            String userId = getCurrentUserId();
            List<Order> orders = orderService.getOrdersByUserId(userId, page, pageSize);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 预订行程
     */
    @PostMapping("/book")
    public Result<String> bookTrip(@RequestBody Map<String, String> request) {
        try {
            String tripId = request.get("tripId");
            String userId = getCurrentUserId();
            String orderId = orderService.bookTrip(tripId, userId);
            return Result.success(orderId);
        } catch (Exception e) {
            return Result.error("预订行程失败: " + e.getMessage());
        }
    }

    /**
     * 创建订单并生成支付参数
     */
    @PostMapping("/create")
    public Result<OrderPaymentResponse> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            String userId = getCurrentUserId();
            OrderPaymentResponse response = orderService.createOrderWithPayment(request, userId);
            if (response != null) {
                return Result.success(response);
            } else {
                return Result.error("创建订单失败");
            }
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付回调接口
     */
    @PostMapping("/payment/wechat/notify")
    public String wechatPayNotify(@RequestBody Map<String, String> notifyData) {
        try {
            boolean success = orderService.handleWechatPayNotify(notifyData);
            return WechatPayUtils.buildNotifyResponse(success, success ? "OK" : "处理失败");
        } catch (Exception e) {
            return WechatPayUtils.buildNotifyResponse(false, "异常: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Result<Boolean> cancelOrder(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String userId = getCurrentUserId();
            boolean success = orderService.cancelOrder(orderId, userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 评价订单
     */
    @PostMapping("/rate")
    public Result<Boolean> rateOrder(@RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            Integer rating = (Integer) request.get("rating");
            String feedback = (String) request.get("feedback");
            String userId = getCurrentUserId();

            boolean success = orderService.rateOrder(orderId, userId, rating, feedback);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("评价订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<Order> getOrderById(@PathVariable String orderId) {
        try {
            String userId = getCurrentUserId();
            Order order = orderService.getOrderById(orderId, userId);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("订单不存在");
            }
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单状态
     */
    @GetMapping("/{orderId}/status")
    public Result<String> getOrderStatus(@PathVariable String orderId) {
        try {
            String userId = getCurrentUserId();
            String status = orderService.getOrderStatus(orderId, userId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("获取订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID（实际项目中应该从JWT token或session中获取）
     */
    private String getCurrentUserId() {
        // 这里应该从认证信息中获取真实的用户ID
        // 为了演示，返回一个固定的用户ID
        return userService.getCurrentUserId();
    }
}