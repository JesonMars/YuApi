package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Order;
import com.pingo.yuapi.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.pingo.yuapi.utils.IdGeneratorUtils;

@Service
public class OrderServiceImpl implements OrderService {

    // 模拟订单存储
    private Map<String, Order> orderStorage = new HashMap<>();

    public OrderServiceImpl() {
        // 初始化一些示例订单数据
        initSampleOrders();
    }

    @Override
    public List<Order> getOrdersByUserId(String userId, int page, int pageSize) {
        // 过滤当前用户的订单，按时间倒序排序
        List<Order> userOrders = orderStorage.values().stream()
                .filter(order -> userId.equals(order.getPassengerId()))
                .filter(order -> isWithinLastWeek(order.getCreateTime()))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());

        // 分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, userOrders.size());

        if (start >= userOrders.size()) {
            return new ArrayList<>();
        }

        return userOrders.subList(start, end);
    }

    @Override
    public String bookTrip(String tripId, String userId) {
        String orderId = "order_" + IdGeneratorUtils.generateShortId();

        Order order = new Order();
        order.setId(orderId);
        order.setTripId(tripId);
        order.setPassengerId(userId);
        order.setDriverId("driver_" + tripId.substring(tripId.length() - 3));
        order.setDriverName("司机" + tripId.substring(tripId.length() - 2));
        order.setDriverAvatar("/static/driver-avatar.png");
        order.setRoute("荣盛阿尔卡迪亚 → 建国门");
        order.setDepartureTime(LocalDateTime.now().plusHours(2));
        order.setStatus("pending");
        order.setPrice(new BigDecimal("38"));
        order.setCreateTime(LocalDateTime.now());
        order.setVehicleInfo("白色 特斯拉 Model 3");
        order.setUpdateTime(LocalDateTime.now());

        orderStorage.put(orderId, order);

        return orderId;
    }

    @Override
    public boolean cancelOrder(String orderId, String userId) {
        Order order = orderStorage.get(orderId);
        if (order != null && userId.equals(order.getPassengerId())) {
            order.setStatus("cancelled");
            order.setUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public boolean rateOrder(String orderId, String userId, Integer rating, String feedback) {
        Order order = orderStorage.get(orderId);
        if (order != null && userId.equals(order.getPassengerId())) {
            order.setRating(rating);
            order.setFeedback(feedback);
            order.setUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public Order getOrderById(String orderId, String userId) {
        Order order = orderStorage.get(orderId);
        if (order != null && userId.equals(order.getPassengerId())) {
            return order;
        }
        return null;
    }

    @Override
    public String getOrderStatus(String orderId, String userId) {
        Order order = getOrderById(orderId, userId);
        return order != null ? order.getStatus() : null;
    }

    /**
     * 检查订单是否在最近一周内
     */
    private boolean isWithinLastWeek(LocalDateTime createTime) {
        return createTime.isAfter(LocalDateTime.now().minusWeeks(1));
    }

    /**
     * 初始化示例订单数据
     */
    private void initSampleOrders() {
        LocalDateTime baseTime = LocalDateTime.now();

        // 示例订单1 - 已完成
        Order order1 = new Order();
        order1.setId("order_001");
        order1.setTripId("trip_001");
        order1.setPassengerId("user_001");
        order1.setDriverId("driver_001");
        order1.setDriverName("张师傅");
        order1.setDriverAvatar("/static/driver1.png");
        order1.setRoute("荣盛阿尔卡迪亚 → 建国门");
        order1.setDepartureTime(baseTime.minusHours(3));
        order1.setStatus("completed");
        order1.setPrice(new BigDecimal("38"));
        order1.setCreateTime(baseTime.minusHours(4));
        order1.setVehicleInfo("白色 特斯拉 Model 3");
        order1.setUpdateTime(baseTime.minusHours(1));
        orderStorage.put("order_001", order1);

        // 示例订单2 - 待确认
        Order order2 = new Order();
        order2.setId("order_002");
        order2.setTripId("trip_002");
        order2.setPassengerId("user_001");
        order2.setDriverId("driver_002");
        order2.setDriverName("李师傅");
        order2.setDriverAvatar("/static/driver2.png");
        order2.setRoute("建国门 → 廊坊市荣盛阿尔卡迪亚");
        order2.setDepartureTime(baseTime.plusHours(2));
        order2.setStatus("pending");
        order2.setPrice(new BigDecimal("42"));
        order2.setCreateTime(baseTime.minusMinutes(30));
        order2.setVehicleInfo("黑色 比亚迪 汉");
        order2.setUpdateTime(baseTime.minusMinutes(30));
        orderStorage.put("order_002", order2);

        // 示例订单3 - 已确认
        Order order3 = new Order();
        order3.setId("order_003");
        order3.setTripId("trip_003");
        order3.setPassengerId("user_001");
        order3.setDriverId("driver_003");
        order3.setDriverName("王师傅");
        order3.setDriverAvatar("/static/driver3.png");
        order3.setRoute("永安里地铁站 → 香河");
        order3.setDepartureTime(baseTime.plusHours(6));
        order3.setStatus("confirmed");
        order3.setPrice(new BigDecimal("35"));
        order3.setCreateTime(baseTime.minusHours(1));
        order3.setVehicleInfo("蓝色 大众 朗逸");
        order3.setUpdateTime(baseTime.minusMinutes(45));
        orderStorage.put("order_003", order3);
    }
}