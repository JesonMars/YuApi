package com.pingo.yuapi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserCommuteConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;

    // 途经点配置（按timing区分）
    private String timing; // "tonight" or "tomorrow"
    private String pickupPoints; // JSON string
    private String dropoffPoints; // JSON string

    // 司机默认配置
    private Integer defaultSeatCount; // 默认座位数
    private Long defaultPricePerSeat; // 默认单价/位（分）
    private Boolean defaultRecurring; // 是否循环类型

    private String defaultRecurringType; // 默认循环类型
    private String defaultNotes; // 默认备注

    // 乘客默认配置
    private Integer defaultPassengerCount; // 默认乘车人数
    private Long defaultOfferPrice; // 默认出价（分）

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getPickupPoints() {
        return pickupPoints;
    }

    public void setPickupPoints(String pickupPoints) {
        this.pickupPoints = pickupPoints;
    }

    public String getDropoffPoints() {
        return dropoffPoints;
    }

    public void setDropoffPoints(String dropoffPoints) {
        this.dropoffPoints = dropoffPoints;
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

    public Integer getDefaultSeatCount() {
        return defaultSeatCount;
    }

    public void setDefaultSeatCount(Integer defaultSeatCount) {
        this.defaultSeatCount = defaultSeatCount;
    }

    public Long getDefaultPricePerSeat() {
        return defaultPricePerSeat;
    }

    public void setDefaultPricePerSeat(Long defaultPricePerSeat) {
        this.defaultPricePerSeat = defaultPricePerSeat;
    }

    public Boolean getDefaultRecurring() {
        return defaultRecurring;
    }

    public void setDefaultRecurring(Boolean defaultRecurring) {
        this.defaultRecurring = defaultRecurring;
    }

    public String getDefaultRecurringType() {
        return defaultRecurringType;
    }

    public void setDefaultRecurringType(String defaultRecurringType) {
        this.defaultRecurringType = defaultRecurringType;
    }

    public String getDefaultNotes() {
        return defaultNotes;
    }

    public void setDefaultNotes(String defaultNotes) {
        this.defaultNotes = defaultNotes;
    }

    public Integer getDefaultPassengerCount() {
        return defaultPassengerCount;
    }

    public void setDefaultPassengerCount(Integer defaultPassengerCount) {
        this.defaultPassengerCount = defaultPassengerCount;
    }

    public Long getDefaultOfferPrice() {
        return defaultOfferPrice;
    }

    public void setDefaultOfferPrice(Long defaultOfferPrice) {
        this.defaultOfferPrice = defaultOfferPrice;
    }
}
