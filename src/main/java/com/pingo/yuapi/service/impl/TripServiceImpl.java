package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Trip;
import com.pingo.yuapi.service.TripService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    // 模拟数据存储
    private Map<String, Trip> tripStorage = new HashMap<>();
    
    public TripServiceImpl() {
        // 初始化一些示例数据
        initSampleData();
    }

    @Override
    public List<Trip> getTripList(Map<String, Object> params) {
        List<Trip> allTrips = new ArrayList<>(tripStorage.values());
        
        // 根据参数进行过滤
        return filterTrips(allTrips, params);
    }

    @Override
    public List<Trip> getTodayTrips(Map<String, Object> params) {
        LocalDateTime now = LocalDateTime.now();
        
        List<Trip> todayTrips = tripStorage.values().stream()
            .filter(trip -> {
                LocalDateTime departureTime = trip.getDepartureTime();
                // 只返回今天且时间在当前时间之后的行程
                return departureTime.toLocalDate().equals(now.toLocalDate()) 
                       && departureTime.isAfter(now);
            })
            .sorted((t1, t2) -> t1.getDepartureTime().compareTo(t2.getDepartureTime()))
            .collect(Collectors.toList());
        
        return filterTrips(todayTrips, params);
    }

    @Override
    public String createTrip(Trip trip) {
        String tripId = UUID.randomUUID().toString();
        trip.setId(tripId);
        trip.setCreateTime(LocalDateTime.now());
        trip.setUpdateTime(LocalDateTime.now());
        trip.setStatus("available");
        
        // 设置默认值
        if (trip.getDriverName() == null) {
            trip.setDriverName("用户" + tripId.substring(0, 6));
        }
        if (trip.getDriverAvatar() == null) {
            trip.setDriverAvatar("/static/default-avatar.png");
        }
        if (trip.getAvailableSeats() == null) {
            trip.setAvailableSeats(trip.getPassengerCount() != null ? trip.getPassengerCount() : 1);
        }
        
        tripStorage.put(tripId, trip);
        return tripId;
    }

    @Override
    public Trip getTripById(String tripId) {
        return tripStorage.get(tripId);
    }

    @Override
    public boolean updateTrip(Trip trip) {
        if (tripStorage.containsKey(trip.getId())) {
            trip.setUpdateTime(LocalDateTime.now());
            tripStorage.put(trip.getId(), trip);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTrip(String tripId) {
        return tripStorage.remove(tripId) != null;
    }

    @Override
    public List<Trip> searchNearbyTrips(Map<String, Object> searchParams) {
        // 根据位置参数搜索附近的行程
        List<Trip> allTrips = new ArrayList<>(tripStorage.values());
        return filterTrips(allTrips, searchParams);
    }

    /**
     * 根据参数过滤行程
     */
    private List<Trip> filterTrips(List<Trip> trips, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return trips;
        }

        return trips.stream()
            .filter(trip -> {
                // 按类型过滤
                String type = (String) params.get("type");
                if (type != null && !type.equals(trip.getType())) {
                    return false;
                }
                
                // 按出发时间段过滤
                String timeRange = (String) params.get("departureTimeRange");
                if (timeRange != null && !timeRange.equals("不限")) {
                    LocalTime tripTime = trip.getDepartureTime().toLocalTime();
                    if (!isTimeInRange(tripTime, timeRange)) {
                        return false;
                    }
                }
                
                // 按状态过滤
                String status = (String) params.get("status");
                if (status != null && !status.equals(trip.getStatus())) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }

    /**
     * 检查时间是否在指定范围内
     */
    private boolean isTimeInRange(LocalTime time, String timeRange) {
        try {
            String[] parts = timeRange.split("-");
            if (parts.length == 2) {
                LocalTime start = LocalTime.parse(parts[0]);
                LocalTime end = LocalTime.parse(parts[1]);
                return !time.isBefore(start) && time.isBefore(end);
            }
        } catch (Exception e) {
            // 解析失败，不过滤
        }
        return true;
    }

    /**
     * 初始化示例数据
     */
    private void initSampleData() {
        LocalDateTime baseTime = LocalDateTime.now();
        
        // 示例行程1
        Trip trip1 = new Trip();
        trip1.setId("trip_001");
        trip1.setDriverId("driver_001");
        trip1.setDriverName("张师傅");
        trip1.setDriverAvatar("/static/avatar1.png");
        trip1.setStartLocation("荣盛阿尔卡迪亚·花语城七地块");
        trip1.setEndLocation("北京市建国门");
        trip1.setDepartureTime(baseTime.plusHours(2));
        trip1.setAvailableSeats(3);
        trip1.setPrice(new BigDecimal("38"));
        trip1.setVehicleInfo("特斯拉 Model 3 白色");
        trip1.setStatus("available");
        trip1.setType("car_seeking_people");
        trip1.setCreateTime(baseTime.minusHours(1));
        tripStorage.put("trip_001", trip1);

        // 示例行程2
        Trip trip2 = new Trip();
        trip2.setId("trip_002");
        trip2.setDriverId("driver_002");
        trip2.setDriverName("李师傅");
        trip2.setDriverAvatar("/static/avatar2.png");
        trip2.setStartLocation("北京市建国门");
        trip2.setEndLocation("廊坊市荣盛阿尔卡迪亚");
        trip2.setDepartureTime(baseTime.plusHours(3));
        trip2.setAvailableSeats(2);
        trip2.setPrice(new BigDecimal("42"));
        trip2.setVehicleInfo("比亚迪 汉 黑色");
        trip2.setStatus("available");
        trip2.setType("car_seeking_people");
        trip2.setCreateTime(baseTime.minusMinutes(30));
        tripStorage.put("trip_002", trip2);

        // 示例行程3 - 已过期的行程
        Trip trip3 = new Trip();
        trip3.setId("trip_003");
        trip3.setDriverId("driver_003");
        trip3.setDriverName("王师傅");
        trip3.setDriverAvatar("/static/avatar3.png");
        trip3.setStartLocation("永安里地铁站");
        trip3.setEndLocation("廊坊市香河");
        trip3.setDepartureTime(baseTime.minusHours(1)); // 已过期
        trip3.setAvailableSeats(1);
        trip3.setPrice(new BigDecimal("35"));
        trip3.setVehicleInfo("大众 朗逸 蓝色");
        trip3.setStatus("completed");
        trip3.setType("car_seeking_people");
        trip3.setCreateTime(baseTime.minusHours(2));
        tripStorage.put("trip_003", trip3);
    }
}