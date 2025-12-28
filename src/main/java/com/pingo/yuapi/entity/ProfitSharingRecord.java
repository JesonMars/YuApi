package com.pingo.yuapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分账记录实体
 */
public class ProfitSharingRecord {
    private String id;
    private String orderId; // 关联订单ID
    private String outOrderNo; // 商户分账单号
    private String transactionId; // 微信订单号
    private String status; // pending(待分账), processing(分账中), finished(已完成), closed(已关闭)

    // 分账金额
    private BigDecimal totalAmount; // 订单总金额
    private BigDecimal driverAmount; // 司机分账金额
    private BigDecimal platformAmount; // 平台分账金额
    private BigDecimal promoterAmount; // 推广员分账金额（如果有）

    // 分账接收方
    private String driverReceiverId; // 司机分账接收方ID
    private String platformReceiverId; // 平台分账接收方ID
    private String promoterReceiverId; // 推广员分账接收方ID

    // 微信返回字段
    private String profitSharingOrderId; // 微信分账单号
    private String errorDescription; // 失败原因

    private LocalDateTime createTime;
    private LocalDateTime finishTime; // 分账完成时间
    private LocalDateTime updateTime;

    public ProfitSharingRecord() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDriverAmount() {
        return driverAmount;
    }

    public void setDriverAmount(BigDecimal driverAmount) {
        this.driverAmount = driverAmount;
    }

    public BigDecimal getPlatformAmount() {
        return platformAmount;
    }

    public void setPlatformAmount(BigDecimal platformAmount) {
        this.platformAmount = platformAmount;
    }

    public BigDecimal getPromoterAmount() {
        return promoterAmount;
    }

    public void setPromoterAmount(BigDecimal promoterAmount) {
        this.promoterAmount = promoterAmount;
    }

    public String getDriverReceiverId() {
        return driverReceiverId;
    }

    public void setDriverReceiverId(String driverReceiverId) {
        this.driverReceiverId = driverReceiverId;
    }

    public String getPlatformReceiverId() {
        return platformReceiverId;
    }

    public void setPlatformReceiverId(String platformReceiverId) {
        this.platformReceiverId = platformReceiverId;
    }

    public String getPromoterReceiverId() {
        return promoterReceiverId;
    }

    public void setPromoterReceiverId(String promoterReceiverId) {
        this.promoterReceiverId = promoterReceiverId;
    }

    public String getProfitSharingOrderId() {
        return profitSharingOrderId;
    }

    public void setProfitSharingOrderId(String profitSharingOrderId) {
        this.profitSharingOrderId = profitSharingOrderId;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
