package com.pingo.yuapi.service;

import com.pingo.yuapi.dto.CreateOrderRequest;
import com.pingo.yuapi.dto.OrderPaymentResponse;
import com.pingo.yuapi.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * 根据用户ID获取订单列表
     */
    List<Order> getOrdersByUserId(String userId, int page, int pageSize);

    /**
     * 预订行程
     */
    String bookTrip(String tripId, String userId);

    /**
     * 创建订单并生成支付参数
     */
    OrderPaymentResponse createOrderWithPayment(CreateOrderRequest request, String userId);

    /**
     * 处理微信支付回调
     */
    boolean handleWechatPayNotify(Map<String, String> notifyData);

    /**
     * 取消订单
     */
    boolean cancelOrder(String orderId, String userId);

    /**
     * 评价订单
     */
    boolean rateOrder(String orderId, String userId, Integer rating, String feedback);

    /**
     * 根据ID获取订单
     */
    Order getOrderById(String orderId, String userId);

    /**
     * 获取订单状态
     */
    String getOrderStatus(String orderId, String userId);

    /**
     * 根据商户订单号获取订单
     */
    Order getOrderByOutTradeNo(String outTradeNo);
}