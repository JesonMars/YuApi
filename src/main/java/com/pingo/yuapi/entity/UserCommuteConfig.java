package com.pingo.yuapi.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserCommuteConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String timing; // "tonight" or "tomorrow"
    private String pickupPoints; // JSON string
    private String dropoffPoints; // JSON string
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
}
