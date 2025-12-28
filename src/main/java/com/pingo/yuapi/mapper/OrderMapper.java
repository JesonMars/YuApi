package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据访问层接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据用户ID查询订单列表（最近一周）
     */
    List<Order> selectOrdersByUserId(@Param("userId") String userId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 根据订单ID查询订单
     */
    Order selectOrderById(@Param("id") String id);

    /**
     * 根据商户订单号查询订单
     */
    Order selectOrderByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 插入订单
     */
    int insertOrder(Order order);

    /**
     * 更新订单
     */
    int updateOrder(Order order);

    /**
     * 更新订单状态
     */
    int updateOrderStatus(@Param("id") String id,
                         @Param("status") String status,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新支付状态
     */
    int updatePaymentStatus(@Param("id") String id,
                           @Param("paymentStatus") String paymentStatus,
                           @Param("transactionId") String transactionId,
                           @Param("payTime") LocalDateTime payTime,
                           @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新订单评价
     */
    int updateOrderRating(@Param("id") String id,
                         @Param("rating") Integer rating,
                         @Param("feedback") String feedback,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询订单总数
     */
    int countOrdersByUserId(@Param("userId") String userId,
                           @Param("startTime") LocalDateTime startTime);
}
