package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.dto.CreateOrderRequest;
import com.pingo.yuapi.dto.OrderPaymentResponse;
import com.pingo.yuapi.entity.Order;
import com.pingo.yuapi.mapper.OrderMapper;
import com.pingo.yuapi.service.OrderService;
import com.pingo.yuapi.service.TripService;
import com.pingo.yuapi.service.AccountService;
import com.pingo.yuapi.entity.Trip;
import com.pingo.yuapi.utils.IdGeneratorUtils;
import com.pingo.yuapi.utils.WechatPayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired(required = false)
    private TripService tripService;

    @Autowired(required = false)
    private AccountService accountService;

    @Override
    public List<Order> getOrdersByUserId(String userId, int page, int pageSize) {
        try {
            // 查询最近一周的订单
            LocalDateTime startTime = LocalDateTime.now().minusWeeks(1);
            int offset = (page - 1) * pageSize;

            List<Order> orders = orderMapper.selectOrdersByUserId(userId, startTime, offset, pageSize);
            logger.info("查询用户订单成功: userId={}, page={}, count={}", userId, page, orders.size());

            return orders != null ? orders : new ArrayList<>();
        } catch (Exception e) {
            logger.error("查询用户订单失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public String bookTrip(String tripId, String userId) {
        try {
            String orderId = "order_" + IdGeneratorUtils.generateShortId();

            // 获取行程信息
            Trip trip = null;
            if (tripService != null) {
                trip = tripService.getTripById(tripId);
                if (trip == null) {
                    logger.error("行程不存在: tripId={}", tripId);
                    return null;
                }
            }

            Order order = new Order();
            order.setId(orderId);
            order.setTripId(tripId);
            order.setPassengerId(userId);
            order.setStatus("pending");
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 从行程中获取信息
            if (trip != null) {
                order.setDriverId(trip.getUserId());
                order.setRoute(trip.getStartLocation() + " → " + trip.getEndLocation());
                order.setDepartureTime(trip.getDepartureTime());
                order.setPrice(new BigDecimal(trip.getPrice()).divide(new BigDecimal("100"))); // 价格从分转为元
            }

            // 插入数据库
            int result = orderMapper.insertOrder(order);
            if (result > 0) {
                logger.info("预订行程成功: orderId={}, tripId={}", orderId, tripId);
                return orderId;
            } else {
                logger.error("预订行程失败: 数据库插入失败");
                return null;
            }
        } catch (Exception e) {
            logger.error("预订行程失败: tripId={}", tripId, e);
            return null;
        }
    }

    @Override
    public boolean cancelOrder(String orderId, String userId) {
        try {
            // 验证订单是否存在且属于该用户
            Order order = orderMapper.selectOrderById(orderId);
            if (order == null) {
                logger.error("订单不存在: orderId={}", orderId);
                return false;
            }

            if (!userId.equals(order.getPassengerId())) {
                logger.error("订单不属于该用户: orderId={}, userId={}", orderId, userId);
                return false;
            }

            // 更新订单状态为已取消
            int result = orderMapper.updateOrderStatus(orderId, "cancelled", LocalDateTime.now());
            if (result > 0) {
                logger.info("取消订单成功: orderId={}", orderId);
                return true;
            } else {
                logger.error("取消订单失败: 数据库更新失败");
                return false;
            }
        } catch (Exception e) {
            logger.error("取消订单失败: orderId={}", orderId, e);
            return false;
        }
    }

    @Override
    public boolean rateOrder(String orderId, String userId, Integer rating, String feedback) {
        try {
            // 验证订单是否存在且属于该用户
            Order order = orderMapper.selectOrderById(orderId);
            if (order == null) {
                logger.error("订单不存在: orderId={}", orderId);
                return false;
            }

            if (!userId.equals(order.getPassengerId())) {
                logger.error("订单不属于该用户: orderId={}, userId={}", orderId, userId);
                return false;
            }

            // 更新订单评价
            int result = orderMapper.updateOrderRating(orderId, rating, feedback, LocalDateTime.now());
            if (result > 0) {
                logger.info("评价订单成功: orderId={}, rating={}", orderId, rating);
                return true;
            } else {
                logger.error("评价订单失败: 数据库更新失败");
                return false;
            }
        } catch (Exception e) {
            logger.error("评价订单失败: orderId={}", orderId, e);
            return false;
        }
    }

    @Override
    public Order getOrderById(String orderId, String userId) {
        try {
            Order order = orderMapper.selectOrderById(orderId);
            if (order != null && userId.equals(order.getPassengerId())) {
                return order;
            }
            logger.warn("订单不存在或不属于该用户: orderId={}, userId={}", orderId, userId);
            return null;
        } catch (Exception e) {
            logger.error("查询订单详情失败: orderId={}", orderId, e);
            return null;
        }
    }

    @Override
    public String getOrderStatus(String orderId, String userId) {
        Order order = getOrderById(orderId, userId);
        return order != null ? order.getStatus() : null;
    }

    @Override
    public OrderPaymentResponse createOrderWithPayment(CreateOrderRequest request, String userId) {
        try {
            // 1. 生成订单ID和商户订单号
            String orderId = "order_" + IdGeneratorUtils.generateShortId();
            String outTradeNo = "OUT" + System.currentTimeMillis() + IdGeneratorUtils.generateShortId();

            // 2. 获取行程信息
            Trip trip = null;
            if (tripService != null) {
                trip = tripService.getTripById(request.getTripId());
            }

            // 3. 创建订单
            Order order = new Order();
            order.setId(orderId);
            order.setTripId(request.getTripId());
            order.setPassengerId(userId);
            order.setPickupPoint(request.getPickupPoint());
            order.setDropoffPoint(request.getDropoffPoint());
            order.setPassengerCount(request.getPassengerCount());
            order.setPrice(request.getTotalAmount());
            order.setStatus("pending");
            order.setPaymentMethod("wechat");
            order.setPaymentStatus("unpaid");
            order.setOutTradeNo(outTradeNo);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 设置行程相关信息
            if (trip != null) {
                order.setDriverId(trip.getUserId());
                order.setRoute(trip.getStartLocation() + " → " + trip.getEndLocation());
                order.setDepartureTime(trip.getDepartureTime());
                // 可以从 trip 获取司机信息
            } else {
                // 模拟数据
                order.setDriverId("driver_001");
                order.setDriverName("司机");
                order.setRoute(request.getPickupPoint() + " → " + request.getDropoffPoint());
                order.setDepartureTime(LocalDateTime.now().plusHours(2));
            }

            // 4. 生成微信支付参数
            int totalFee = request.getTotalAmount().multiply(new BigDecimal("100")).intValue(); // 转换为分
            String body = "拼车订单-" + order.getRoute();
            String openid = request.getOpenid() != null ? request.getOpenid() : "mock_openid";

            Map<String, Object> paymentParams = WechatPayUtils.generateJsapiPayParams(
                    outTradeNo,
                    totalFee,
                    body,
                    openid);

            if (paymentParams == null) {
                logger.error("生成微信支付参数失败");
                return null;
            }

            // 5. 保存订单到数据库
            int result = orderMapper.insertOrder(order);
            if (result <= 0) {
                logger.error("保存订单失败: 数据库插入失败");
                return null;
            }

            logger.info("创建订单成功: orderId={}, outTradeNo={}", orderId, outTradeNo);

            // 6. 返回支付参数
            return new OrderPaymentResponse(orderId, outTradeNo, paymentParams);

        } catch (Exception e) {
            logger.error("创建订单失败", e);
            return null;
        }
    }

    @Override
    public boolean handleWechatPayNotify(Map<String, String> notifyData) {
        try {
            // 1. 验证签名
            if (!WechatPayUtils.verifyNotifySign(notifyData)) {
                logger.error("微信支付回调签名验证失败");
                return false;
            }

            // 2. 检查支付结果
            String returnCode = notifyData.get("return_code");
            String resultCode = notifyData.get("result_code");
            if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                logger.error("微信支付失败: return_code={}, result_code={}", returnCode, resultCode);
                return false;
            }

            // 3. 获取订单号
            String outTradeNo = notifyData.get("out_trade_no");
            String transactionId = notifyData.get("transaction_id");

            // 4. 查找订单
            Order order = getOrderByOutTradeNo(outTradeNo);
            if (order == null) {
                logger.error("订单不存在: outTradeNo={}", outTradeNo);
                return false;
            }

            // 5. 检查订单是否已支付
            if ("paid".equals(order.getPaymentStatus())) {
                logger.warn("订单已支付，忽略重复通知: orderId={}", order.getId());
                return true;
            }

            // 6. 更新订单支付状态
            LocalDateTime now = LocalDateTime.now();
            int result = orderMapper.updatePaymentStatus(
                    order.getId(),
                    "paid",
                    transactionId,
                    now,
                    now
            );

            if (result <= 0) {
                logger.error("更新订单支付状态失败: orderId={}", order.getId());
                return false;
            }

            // 更新订单状态为已确认
            orderMapper.updateOrderStatus(order.getId(), "confirmed", now);

            logger.info("订单支付成功: orderId={}, transactionId={}", order.getId(), transactionId);

            // 7. 将订单金额添加到司机账户（作为冻结余额，T+1可提现）
            if (accountService != null && order.getDriverId() != null) {
                try {
                    accountService.addIncome(order.getDriverId(), order.getId(), order.getPrice());
                    logger.info("订单收入已添加到司机账户: driverId={}, orderId={}, amount={}",
                            order.getDriverId(), order.getId(), order.getPrice());
                } catch (Exception e) {
                    logger.error("添加司机收入失败: driverId={}, orderId={}", order.getDriverId(), order.getId(), e);
                    // 这里不影响订单状态，可以后续补偿
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("处理微信支付回调失败", e);
            return false;
        }
    }

    @Override
    public Order getOrderByOutTradeNo(String outTradeNo) {
        try {
            return orderMapper.selectOrderByOutTradeNo(outTradeNo);
        } catch (Exception e) {
            logger.error("根据商户订单号查询订单失败: outTradeNo={}", outTradeNo, e);
            return null;
        }
    }
}