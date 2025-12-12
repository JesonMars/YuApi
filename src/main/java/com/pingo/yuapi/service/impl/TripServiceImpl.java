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
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    @Autowired
    private TripMapper tripMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Trip> getTripList(Map<String, Object> params) {
        // 使用数据库查询，并按出发时间排序
        List<Trip> trips;
        if (params == null || params.isEmpty()) {
            trips = tripMapper.selectAllTrips();
        } else {
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
            
            trips = tripMapper.selectTripsByCondition(queryParams);
        }
        
        // 应用额外的筛选逻辑（地铁站、关注、距离等）
        return filterTripsAdditional(trips, params);
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

        // 用户家和公司的坐标（这里使用固定坐标，实际应该从用户设置中获取）
        double homeLat = 39.5283;  // 荣盛阿尔卡迪亚·花语城纬度
        double homeLng = 116.7428; // 荣盛阿尔卡迪亚·花语城经度
        double companyLat = 39.9081; // 国贸CBD纬度
        double companyLng = 116.4609; // 国贸CBD经度
        
        // 获取已关注的用户列表（如果需要）
        Set<String> followedUserIds = null;
        Object followsOnlyObj = params.get("followsOnly");
        if (followsOnlyObj != null && (Boolean.parseBoolean(followsOnlyObj.toString()) || "true".equals(followsOnlyObj.toString()))) {
            followedUserIds = getFollowedUserIds((String) params.get("currentUserId"));
        }

        final Set<String> finalFollowedUserIds = followedUserIds;
        
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
                
                // 家附近距离筛选
                Object homeDistanceObj = params.get("homeDistance");
                if (homeDistanceObj != null) {
                    try {
                        int homeDistance = homeDistanceObj instanceof String ? 
                            Integer.parseInt((String) homeDistanceObj) : (Integer) homeDistanceObj;
                        if (homeDistance > 0 && trip.getStartLatitude() != null && trip.getStartLongitude() != null) {
                            double distance = calculateDistance(homeLat, homeLng, 
                                trip.getStartLatitude(), trip.getStartLongitude());
                            if (distance > homeDistance) {
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的距离参数
                    }
                }
                
                // 公司附近距离筛选
                Object companyDistanceObj = params.get("companyDistance");
                if (companyDistanceObj != null) {
                    try {
                        int companyDistance = companyDistanceObj instanceof String ? 
                            Integer.parseInt((String) companyDistanceObj) : (Integer) companyDistanceObj;
                        if (companyDistance > 0 && trip.getStartLatitude() != null && trip.getStartLongitude() != null) {
                            double distance = calculateDistance(companyLat, companyLng, 
                                trip.getStartLatitude(), trip.getStartLongitude());
                            if (distance > companyDistance) {
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的距离参数
                    }
                }
                
                // 关注筛选
                if (finalFollowedUserIds != null && !finalFollowedUserIds.isEmpty()) {
                    if (!finalFollowedUserIds.contains(trip.getDriverId())) {
                        return false;
                    }
                }
                
                // 地铁站筛选
                Object subwayStationsObj = params.get("subwayStations");
                List<String> subwayStations = null;
                if (subwayStationsObj instanceof String) {
                    String stationsStr = (String) subwayStationsObj;
                    if (!stationsStr.isEmpty()) {
                        subwayStations = Arrays.asList(stationsStr.split(","));
                    }
                } else if (subwayStationsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> stationsList = (List<String>) subwayStationsObj;
                    subwayStations = stationsList;
                }
                
                if (subwayStations != null && !subwayStations.isEmpty()) {
                    boolean foundMatch = false;
                    String startLocation = trip.getStartLocation();
                    String endLocation = trip.getEndLocation();
                    
                    for (String stationId : subwayStations) {
                        String stationName = getSubwayStationName(stationId);
                        if (stationName != null) {
                            if ((startLocation != null && startLocation.contains(stationName)) ||
                                (endLocation != null && endLocation.contains(stationName))) {
                                foundMatch = true;
                                break;
                            }
                        }
                    }
                    
                    if (!foundMatch) {
                        return false;
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

    /**
     * 使用Haversine公式计算两点间的距离（单位：米）
     * @param lat1 第一点纬度
     * @param lng1 第一点经度
     * @param lat2 第二点纬度
     * @param lng2 第二点经度
     * @return 距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // 地球半径（米）
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * 获取用户关注的用户ID列表
     */
    private Set<String> getFollowedUserIds(String currentUserId) {
        Set<String> followedUserIds = new HashSet<>();
        
        if (currentUserId == null || currentUserId.isEmpty()) {
            return followedUserIds;
        }
        
        try {
            // 查询当前用户关注的用户ID列表
            String sql = "SELECT target_user_id FROM user_follows WHERE user_id = ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, currentUserId);
            
            for (Map<String, Object> row : results) {
                followedUserIds.add((String) row.get("target_user_id"));
            }
        } catch (Exception e) {
            logger.error("查询关注用户列表失败: {}", e.getMessage(), e);
        }
        
        return followedUserIds;
    }

    /**
     * 根据地铁站ID获取地铁站名称
     */
    private String getSubwayStationName(String stationId) {
        switch (stationId) {
            case "yonganli": return "永安里";
            case "jianguomen": return "建国门";
            case "beijingzhan": return "北京站";
            case "guomao": return "国贸";
            case "dongdaqiao": return "东大桥";
            case "jintaixizhao": return "金台夕照";
            case "chaoyangmen": return "朝阳门";
            case "guangmenmenwai": return "广渠门外";
            default: return null;
        }
    }
}