package com.pingo.yuapi.service;

import java.util.List;
import java.util.Map;

/**
 * 地图API服务接口
 */
public interface MapApiService {
    
    /**
     * 搜索地址
     * @param city 城市名称
     * @param keyword 搜索关键词
     * @param useCache 是否使用缓存
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchAddress(String city, String keyword, boolean useCache);
    
    /**
     * 地理编码 - 将地址转换为经纬度
     * @param address 地址
     * @param city 城市
     * @return 经纬度信息
     */
    Map<String, Object> geocode(String address, String city);
    
    /**
     * 逆地理编码 - 将经纬度转换为地址
     * @param lng 经度
     * @param lat 纬度
     * @return 地址信息
     */
    Map<String, Object> reverseGeocode(Double lng, Double lat);
}