package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Trip;
import com.pingo.yuapi.mapper.TripMapper;
import com.pingo.yuapi.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TripServiceImpl implements TripService {

    @Autowired
    private TripMapper tripMapper;

    @Override
    public List<Trip> getTripList(Map<String, Object> params) {
        // 使用数据库查询，并按出发时间排序
        if (params == null || params.isEmpty()) {
            return tripMapper.selectAllTrips();
        }
        
        // 构建查询条件
        Map<String, Object> queryParams = new HashMap<>();
        
        // 日期过滤
        if (params.containsKey("date")) {
            queryParams.put("date", params.get("date"));
        }
        
        // 类型过滤
        if (params.containsKey("type")) {
            queryParams.put("type", params.get("type"));
        }
        
        // 状态过滤（默认只查询可用的行程）
        queryParams.put("status", params.getOrDefault("status", "available"));
        
        return tripMapper.selectTripsByCondition(queryParams);
    }

    @Override
    public List<Trip> getTodayTrips(Map<String, Object> params) {
        LocalDateTime now = LocalDateTime.now();
        
        // 从数据库查询今天且时间在当前时间之后的行程
        List<Trip> todayTrips = tripMapper.selectTodayTrips(now);
        
        return filterTripsAdditional(todayTrips, params);
    }

    @Override
    public String createTrip(Trip trip) {
        String tripId = UUID.randomUUID().toString();
        trip.setId(tripId);
        trip.setCreateTime(LocalDateTime.now());
        trip.setUpdateTime(LocalDateTime.now());
        
        if (trip.getStatus() == null) {
            trip.setStatus("available");
        }
        
        // 设置默认值
        if (trip.getDriverName() == null && trip.getDriverId() != null) {
            trip.setDriverName("用户" + trip.getDriverId().substring(0, Math.min(6, trip.getDriverId().length())));
        }
        if (trip.getDriverAvatar() == null) {
            trip.setDriverAvatar("/static/default-avatar.png");
        }
        if (trip.getAvailableSeats() == null) {
            trip.setAvailableSeats(trip.getPassengerCount() != null ? trip.getPassengerCount() : 1);
        }
        
        tripMapper.insertTrip(trip);
        return tripId;
    }

    @Override
    public Trip getTripById(String tripId) {
        return tripMapper.selectTripById(tripId);
    }

    @Override
    public boolean updateTrip(Trip trip) {
        trip.setUpdateTime(LocalDateTime.now());
        return tripMapper.updateTrip(trip) > 0;
    }

    @Override
    public boolean deleteTrip(String tripId) {
        return tripMapper.deleteTripById(tripId) > 0;
    }

    @Override
    public List<Trip> searchNearbyTrips(Map<String, Object> searchParams) {
        // 根据位置参数搜索附近的行程
        Map<String, Object> queryParams = new HashMap<>();
        
        if (searchParams.containsKey("startLocation")) {
            queryParams.put("startLocation", searchParams.get("startLocation"));
        }
        if (searchParams.containsKey("endLocation")) {
            queryParams.put("endLocation", searchParams.get("endLocation"));
        }
        
        queryParams.put("status", "available");
        
        return tripMapper.selectTripsByCondition(queryParams);
    }

    @Override
    public String publishDriverTrip(Map<String, Object> tripData) {
        Trip trip = new Trip();
        String tripId = UUID.randomUUID().toString();
        
        trip.setId(tripId);
        trip.setDriverId((String) tripData.get("driverId"));
        trip.setDriverName((String) tripData.get("driverName"));
        trip.setDriverAvatar((String) tripData.get("driverAvatar"));
        trip.setStartLocation((String) tripData.get("startLocation"));
        trip.setEndLocation((String) tripData.get("endLocation"));
        trip.setDepartureTime(LocalDateTime.parse((String) tripData.get("departureTime")));
        trip.setAvailableSeats(Integer.parseInt(tripData.get("availableSeats").toString()));
        trip.setPrice(new BigDecimal(tripData.get("price").toString()));
        trip.setVehicleInfo((String) tripData.get("vehicleInfo"));
        trip.setNotes((String) tripData.get("note"));
        trip.setType("car_seeking_people");
        trip.setStatus("available");
        trip.setCreateTime(LocalDateTime.now());
        trip.setUpdateTime(LocalDateTime.now());
        
        tripMapper.insertTrip(trip);
        return tripId;
    }

    @Override
    public String publishPassengerTrip(Map<String, Object> tripData) {
        Trip trip = new Trip();
        String tripId = UUID.randomUUID().toString();
        
        trip.setId(tripId);
        trip.setDriverId((String) tripData.get("passengerId")); // 这里用passengerId
        trip.setDriverName((String) tripData.get("passengerName"));
        trip.setDriverAvatar((String) tripData.get("passengerAvatar"));
        trip.setStartLocation((String) tripData.get("startLocation"));
        trip.setEndLocation((String) tripData.get("endLocation"));
        trip.setDepartureTime(LocalDateTime.parse((String) tripData.get("departureTime")));
        trip.setPassengerCount(Integer.parseInt(tripData.get("passengerCount").toString()));
        trip.setPrice(new BigDecimal(tripData.get("pricePerPerson").toString()));
        trip.setNotes((String) tripData.get("note"));
        trip.setType("people_seeking_car");
        trip.setStatus("available");
        trip.setCreateTime(LocalDateTime.now());
        trip.setUpdateTime(LocalDateTime.now());
        
        tripMapper.insertTrip(trip);
        return tripId;
    }

    @Override
    public List<Trip> getUserTrips(String userId, Integer page, Integer limit) {
        int offset = (page - 1) * limit;
        return tripMapper.selectUserTrips(userId, offset, limit);
    }

    @Override
    public boolean joinTrip(String tripId, Map<String, Object> joinData) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip != null && trip.getAvailableSeats() != null && trip.getAvailableSeats() > 0) {
            // 减少可用座位数
            int result = tripMapper.decreaseAvailableSeats(tripId, 1);
            
            if (result > 0) {
                // 检查是否没有可用座位了，更新状态
                Trip updatedTrip = tripMapper.selectTripById(tripId);
                if (updatedTrip.getAvailableSeats() <= 0) {
                    tripMapper.updateTripStatus(tripId, "full");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cancelTrip(String tripId, String reason) {
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip != null) {
            trip.setStatus("cancelled");
            trip.setNotes(trip.getNotes() + " [取消原因: " + reason + "]");
            trip.setUpdateTime(LocalDateTime.now());
            return tripMapper.updateTrip(trip) > 0;
        }
        return false;
    }

    @Override
    public boolean completeTrip(String tripId) {
        return tripMapper.updateTripStatus(tripId, "completed") > 0;
    }

    @Override
    public List<Map<String, Object>> getTripParticipants(String tripId) {
        List<Map<String, Object>> participants = new ArrayList<>();
        
        // 模拟参与者数据（实际应该查询trip_participants表）
        Map<String, Object> participant1 = new HashMap<>();
        participant1.put("id", "user_101");
        participant1.put("name", "乘客1");
        participant1.put("avatar", "/static/passenger1.png");
        participant1.put("phone", "138****1234");
        participant1.put("joinTime", LocalDateTime.now().minusMinutes(30).toString());
        participant1.put("status", "confirmed");
        participants.add(participant1);
        
        return participants;
    }

    /**
     * 额外的前端筛选逻辑（保留原有的筛选功能）
     */
    private List<Trip> filterTripsAdditional(List<Trip> trips, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return trips;
        }

        return trips.stream()
            .filter(trip -> {
                // 按出发时间段过滤
                String timeRange = (String) params.get("departureTimeRange");
                if (timeRange != null && !timeRange.equals("不限")) {
                    LocalDateTime departureTime = trip.getDepartureTime();
                    if (departureTime != null) {
                        String timeStr = String.format("%02d:%02d", 
                            departureTime.getHour(), 
                            departureTime.getMinute());
                        if (!isTimeInRange(timeStr, timeRange)) {
                            return false;
                        }
                    }
                }
                
                return true;
            })
            .collect(ArrayList::new, (list, trip) -> list.add(trip), ArrayList::addAll);
    }

    /**
     * 检查时间是否在指定范围内
     */
    private boolean isTimeInRange(String timeStr, String timeRange) {
        try {
            String[] parts = timeRange.split("-");
            if (parts.length == 2) {
                return timeStr.compareTo(parts[0]) >= 0 && timeStr.compareTo(parts[1]) < 0;
            }
        } catch (Exception e) {
            // 解析失败，不过滤
        }
        return true;
    }
}