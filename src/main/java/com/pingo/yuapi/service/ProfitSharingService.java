package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.ProfitSharingReceiver;
import com.pingo.yuapi.entity.ProfitSharingRecord;

import java.math.BigDecimal;
import java.util.List;

public interface ProfitSharingService {

    /**
     * 添加分账接收方（司机）
     */
    ProfitSharingReceiver addReceiver(String userId, String type, String account, String name, String relationType);

    /**
     * 获取用户的分账接收方信息
     */
    ProfitSharingReceiver getReceiverByUserId(String userId);

    /**
     * 执行分账（订单完成后调用）
     */
    ProfitSharingRecord executeProfitSharing(String orderId, BigDecimal totalAmount, BigDecimal platformRate);

    /**
     * 查询分账记录
     */
    ProfitSharingRecord getProfitSharingRecord(String orderId);

    /**
     * 获取用户的分账记录列表
     */
    List<ProfitSharingRecord> getProfitSharingRecords(String userId, int page, int pageSize);

    /**
     * 完结分账
     */
    boolean finishProfitSharing(String orderId, String description);
}
