package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.Trip;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface TripService {
    
    /**
     * 获取行程列表
     */
    List<Trip> getTripList(Map<String, Object> params);
    
    /**
     * 获取今天的行程（距离当前时间后面的）
     */
    List<Trip> getTodayTrips(Map<String, Object> params);
    
    /**
     * 创建行程
     */
    String createTrip(Trip trip);
    
    /**
     * 获取行程详情
     */
    Trip getTripById(String tripId);
    
    /**
     * 更新行程
     */
    boolean updateTrip(Trip trip);
    
    /**
     * 删除行程
     */
    boolean deleteTrip(String tripId);
    
    /**
     * 搜索附近的行程
     */
    List<Trip> searchNearbyTrips(Map<String, Object> searchParams);
    
    /**
     * 发布司机行程
     */
    String publishDriverTrip(Map<String, Object> tripData);
    
    /**
     * 发布乘客行程
     */
    String publishPassengerTrip(Map<String, Object> tripData);
    
    /**
     * 获取用户行程列表
     */
    List<Trip> getUserTrips(String userId, Integer page, Integer limit);
    
    /**
     * 申请加入行程
     */
    boolean joinTrip(String tripId, Map<String, Object> joinData);
    
    /**
     * 取消行程
     */
    boolean cancelTrip(String tripId, String reason);
    
    /**
     * 完成行程
     */
    boolean completeTrip(String tripId);
    
    /**
     * 获取行程参与者列表
     */
    List<Map<String, Object>> getTripParticipants(String tripId);
}