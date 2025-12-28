package com.pingo.yuapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收入记录实体
 */
public class IncomeRecord {
    private String id;
    private String userId; // 收入所属用户（司机）
    private String orderId; // 关联订单号
    private BigDecimal amount; // 收入金额
    private String status; // available(可提现), frozen(冻结中), withdrawn(已提现)
    private String type; // order(订单收入), refund(退款), other(其他)
    private LocalDateTime incomeTime; // 收入时间
    private LocalDateTime availableTime; // 可提现时间（收入时间+1天）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public IncomeRecord() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getIncomeTime() {
        return incomeTime;
    }

    public void setIncomeTime(LocalDateTime incomeTime) {
        this.incomeTime = incomeTime;
    }

    public LocalDateTime getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(LocalDateTime availableTime) {
        this.availableTime = availableTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
