package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Order;
import com.pingo.yuapi.entity.ProfitSharingReceiver;
import com.pingo.yuapi.entity.ProfitSharingRecord;
import com.pingo.yuapi.service.OrderService;
import com.pingo.yuapi.service.ProfitSharingService;
import com.pingo.yuapi.utils.IdGeneratorUtils;
import com.pingo.yuapi.utils.WechatPayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfitSharingServiceImpl implements ProfitSharingService {

    private static final Logger logger = LoggerFactory.getLogger(ProfitSharingServiceImpl.class);

    // 模拟存储
    private Map<String, ProfitSharingReceiver> receiverStorage = new HashMap<>();
    private Map<String, ProfitSharingRecord> recordStorage = new HashMap<>();

    @Autowired(required = false)
    private OrderService orderService;

    @Override
    public ProfitSharingReceiver addReceiver(String userId, String type, String account, String name, String relationType) {
        try {
            // 1. 检查是否已存在
            ProfitSharingReceiver existing = getReceiverByUserId(userId);
            if (existing != null) {
                logger.warn("用户已存在分账接收方: userId={}", userId);
                return existing;
            }

            // 2. 创建分账接收方
            ProfitSharingReceiver receiver = new ProfitSharingReceiver();
            receiver.setId("receiver_" + IdGeneratorUtils.generateShortId());
            receiver.setUserId(userId);
            receiver.setType(type);
            receiver.setAccount(account);
            receiver.setName(name);
            receiver.setRelationType(relationType);
            receiver.setStatus("active");
            receiver.setCreateTime(LocalDateTime.now());
            receiver.setUpdateTime(LocalDateTime.now());

            // 3. 调用微信接口添加分账接收方
            boolean success = WechatPayUtils.addProfitSharingReceiver(type, account, name, relationType);

            if (success) {
                receiverStorage.put(userId, receiver);
                logger.info("添加分账接收方成功: userId={}, receiverId={}", userId, receiver.getId());
                return receiver;
            } else {
                logger.error("调用微信接口添加分账接收方失败: userId={}", userId);
                return null;
            }

        } catch (Exception e) {
            logger.error("添加分账接收方失败", e);
            return null;
        }
    }

    @Override
    public ProfitSharingReceiver getReceiverByUserId(String userId) {
        return receiverStorage.get(userId);
    }

    @Override
    public ProfitSharingRecord executeProfitSharing(String orderId, BigDecimal totalAmount, BigDecimal platformRate) {
        try {
            // 1. 检查是否已经分账过
            ProfitSharingRecord existing = getProfitSharingRecord(orderId);
            if (existing != null) {
                logger.warn("订单已分账: orderId={}", orderId);
                return existing;
            }

            // 2. 获取订单信息
            Order order = orderService.getOrderById(orderId, "system");
            if (order == null) {
                logger.error("订单不存在: orderId={}", orderId);
                return null;
            }

            if (order.getTransactionId() == null) {
                logger.error("订单未支付: orderId={}", orderId);
                return null;
            }

            // 3. 获取司机分账接收方
            ProfitSharingReceiver driverReceiver = getReceiverByUserId(order.getDriverId());
            if (driverReceiver == null) {
                logger.error("司机未配置分账接收方: driverId={}", order.getDriverId());
                return null;
            }

            // 4. 计算分账金额
            // 平台抽成 = 订单金额 * 平台费率
            // 司机分账 = 订单金额 - 平台抽成
            BigDecimal platformAmount = totalAmount.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal driverAmount = totalAmount.subtract(platformAmount);

            // 5. 创建分账记录
            ProfitSharingRecord record = new ProfitSharingRecord();
            record.setId("ps_" + IdGeneratorUtils.generateShortId());
            record.setOrderId(orderId);
            record.setOutOrderNo("PS" + System.currentTimeMillis() + IdGeneratorUtils.generateShortId());
            record.setTransactionId(order.getTransactionId());
            record.setStatus("processing");
            record.setTotalAmount(totalAmount);
            record.setDriverAmount(driverAmount);
            record.setPlatformAmount(platformAmount);
            record.setPromoterAmount(BigDecimal.ZERO); // 暂不支持推广员
            record.setDriverReceiverId(driverReceiver.getId());
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            // 6. 构建分账接收方列表
            List<Map<String, Object>> receivers = new ArrayList<>();

            // 添加司机分账
            Map<String, Object> driverProfitSharing = new HashMap<>();
            driverProfitSharing.put("type", driverReceiver.getType());
            driverProfitSharing.put("account", driverReceiver.getAccount());
            driverProfitSharing.put("amount", driverAmount.multiply(new BigDecimal("100")).intValue()); // 转为分
            driverProfitSharing.put("description", "司机分账");
            receivers.add(driverProfitSharing);

            // 7. 调用微信分账接口
            Map<String, Object> result = WechatPayUtils.requestProfitSharing(
                    order.getTransactionId(),
                    record.getOutOrderNo(),
                    receivers
            );

            if (result != null && "SUCCESS".equals(result.get("return_code"))) {
                record.setStatus("finished");
                record.setProfitSharingOrderId((String) result.get("order_id"));
                record.setFinishTime(LocalDateTime.now());
                logger.info("分账成功: orderId={}, psRecordId={}", orderId, record.getId());
            } else {
                record.setStatus("closed");
                record.setErrorDescription("分账失败");
                logger.error("分账失败: orderId={}", orderId);
            }

            record.setUpdateTime(LocalDateTime.now());
            recordStorage.put(orderId, record);

            return record;

        } catch (Exception e) {
            logger.error("执行分账失败", e);
            return null;
        }
    }

    @Override
    public ProfitSharingRecord getProfitSharingRecord(String orderId) {
        return recordStorage.get(orderId);
    }

    @Override
    public List<ProfitSharingRecord> getProfitSharingRecords(String userId, int page, int pageSize) {
        // 查询该用户作为司机的所有订单的分账记录
        List<ProfitSharingRecord> userRecords = recordStorage.values().stream()
                .filter(record -> {
                    // 通过 driverReceiverId 关联到司机
                    ProfitSharingReceiver receiver = receiverStorage.values().stream()
                            .filter(r -> r.getId().equals(record.getDriverReceiverId()) && userId.equals(r.getUserId()))
                            .findFirst()
                            .orElse(null);
                    return receiver != null;
                })
                .sorted((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()))
                .collect(Collectors.toList());

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, userRecords.size());

        if (start >= userRecords.size()) {
            return new ArrayList<>();
        }

        return userRecords.subList(start, end);
    }

    @Override
    public boolean finishProfitSharing(String orderId, String description) {
        try {
            ProfitSharingRecord record = getProfitSharingRecord(orderId);
            if (record == null) {
                logger.error("分账记录不存在: orderId={}", orderId);
                return false;
            }

            boolean success = WechatPayUtils.finishProfitSharing(
                    record.getTransactionId(),
                    record.getOutOrderNo(),
                    description
            );

            if (success) {
                logger.info("完结分账成功: orderId={}", orderId);
            }

            return success;

        } catch (Exception e) {
            logger.error("完结分账失败", e);
            return false;
        }
    }
}
