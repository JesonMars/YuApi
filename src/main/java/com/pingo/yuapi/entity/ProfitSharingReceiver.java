package com.pingo.yuapi.entity;

import java.time.LocalDateTime;

/**
 * 分账接收方实体（司机子商户）
 */
public class ProfitSharingReceiver {
    private String id;
    private String userId; // 关联用户ID（司机）
    private String type; // MERCHANT_ID(商户号), PERSONAL_OPENID(个人openid)
    private String account; // 分账接收方账户（子商户号或openid）
    private String name; // 分账接收方姓名
    private String relationType; // SERVICE_PROVIDER(服务商), STORE(门店), STAFF(员工), STORE_OWNER(店主), PARTNER(合作伙伴), HEADQUARTER(总部), BRAND(品牌方), DISTRIBUTOR(分销商), USER(用户), SUPPLIER(供应商)
    private String customRelation; // 自定义关系
    private String status; // active(激活), inactive(未激活), deleted(已删除)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public ProfitSharingReceiver() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getCustomRelation() {
        return customRelation;
    }

    public void setCustomRelation(String customRelation) {
        this.customRelation = customRelation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
