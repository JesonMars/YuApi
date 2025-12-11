package com.pingo.yuapi.entity;

import java.time.LocalDateTime;

/**
 * 地址搜索缓存实体类
 */
public class AddressSearchCache {
    private Long id;
    private String cityName;
    private String keyword;
    private String results;
    private String source;
    private Integer hitCount;
    private LocalDateTime lastHitTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public LocalDateTime getLastHitTime() {
        return lastHitTime;
    }

    public void setLastHitTime(LocalDateTime lastHitTime) {
        this.lastHitTime = lastHitTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}