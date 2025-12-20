package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Trip;
import com.pingo.yuapi.entity.TripDetails;
import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.entity.UserCommuteConfig;
import com.pingo.yuapi.entity.UserLocation;
import com.pingo.yuapi.mapper.TripMapper;
import com.pingo.yuapi.mapper.TripDetailsMapper;
import com.pingo.yuapi.mapper.UserMapper;
import com.pingo.yuapi.mapper.UserCommuteConfigMapper;
import com.pingo.yuapi.mapper.UserLocationMapper;
import com.pingo.yuapi.service.TripService;
import com.pingo.yuapi.utils.DateUtils;
import com.pingo.yuapi.utils.IdGeneratorUtils;
import com.pingo.yuapi.utils.MoneyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    @Autowired
    private TripMapper tripMapper;

    @Autowired
    private TripDetailsMapper tripDetailsMapper;

    @Autowired
    private UserCommuteConfigMapper userCommuteConfigMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLocationMapper userLocationMapper;

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
        logger.info("getTripList() - 筛选前行程数量: {}, 筛选参数: {}", trips.size(), params);
        List<Trip> filteredTrips = filterTripsAdditional(trips, params);
        logger.info("getTripList() - 筛选后行程数量: {}", filteredTrips.size());
        return filteredTrips;
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
        String tripId = IdGeneratorUtils.generateId();
        trip.setId(tripId);

        if (trip.getStatus() == null) {
            trip.setStatus("available");
        }

        // 设置默认值
        if (trip.getBookedSeats() == null) {
            trip.setBookedSeats(0);
        }
        if (trip.getAvailableSeats() == null) {
            trip.setAvailableSeats(3); // 默认3个座位
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
        String userId = (String) tripData.get("userId");
        String timing = (String) tripData.get("timing");
        String tripId = IdGeneratorUtils.generateId();
        String detailsId = IdGeneratorUtils.generateId();

        // 1. 从user_commute_config读取默认配置（如果存在）
        UserCommuteConfig config = userCommuteConfigMapper.findByUserIdAndTiming(userId, timing);
        if (config == null) {
            config = new UserCommuteConfig();
            config.setId(IdGeneratorUtils.generateId());
            config.setUserId(userId);
            config.setTiming(timing);
        }

        // 2. 构建Trip核心数据（精简版）
        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUserId(userId);
        trip.setType("car_seeking_people");

        // 设置起终点信息
        trip.setStartLocation((String) tripData.get("startLocation"));
        trip.setStartCity((String) tripData.get("startCity"));
        trip.setEndLocation((String) tripData.get("endLocation"));
        trip.setEndCity((String) tripData.get("endCity"));

        // 设置经纬度
        if (tripData.get("startLongitude") != null) {
            trip.setStartLongitude(Double.parseDouble(tripData.get("startLongitude").toString()));
        }
        if (tripData.get("startLatitude") != null) {
            trip.setStartLatitude(Double.parseDouble(tripData.get("startLatitude").toString()));
        }
        if (tripData.get("endLongitude") != null) {
            trip.setEndLongitude(Double.parseDouble(tripData.get("endLongitude").toString()));
        }
        if (tripData.get("endLatitude") != null) {
            trip.setEndLatitude(Double.parseDouble(tripData.get("endLatitude").toString()));
        }

        // 设置出发时间
        trip.setDepartureTime(DateUtils.parse((String) tripData.get("departureTime")));
        trip.setTiming(timing);

        // 设置座位数
        Integer seatCount = tripData.get("seatCount") != null ? Integer.parseInt(tripData.get("seatCount").toString())
                : (config.getDefaultSeatCount() != null ? config.getDefaultSeatCount() : 3);
        trip.setAvailableSeats(seatCount);
        trip.setBookedSeats(0);

        // 金额转换：前端传来元，转为分存储
        Long priceFen = null;
        if (tripData.get("pricePerSeat") != null) {
            Double priceYuan = Double.parseDouble(tripData.get("pricePerSeat").toString());
            priceFen = MoneyUtils.yuanToFen(priceYuan);
        } else if (config.getDefaultPricePerSeat() != null) {
            priceFen = config.getDefaultPricePerSeat();
        } else {
            priceFen = 2000L; // 默认20元
        }
        trip.setPrice(priceFen);

        // 设置状态和时间戳
        trip.setStatus("available");

        // 3. 保存trips核心数据
        tripMapper.insertTrip(trip);

        // 4. 构建TripDetails扩展数据
        TripDetails details = new TripDetails();
        details.setId(detailsId);
        details.setTripId(tripId);

        // 途经点（从前端获取，或从配置加载）
        String pickupPoints = (String) tripData.get("pickupPoint");
        String dropoffPoints = (String) tripData.get("dropoffPoint");

        // 获取JSON格式的途经点（包含经纬度）
        String pickupPointsJson = (String) tripData.get("pickupPointsJson");
        String dropoffPointsJson = (String) tripData.get("dropoffPointsJson");

        if (pickupPoints == null && config.getPickupPoints() != null) {
            pickupPoints = config.getPickupPoints();
        }
        if (dropoffPoints == null && config.getDropoffPoints() != null) {
            dropoffPoints = config.getDropoffPoints();
        }

        // TripDetails 目前还是存储简单的字符串（或者也可以存JSON，视需求而定，这里保持原样或存JSON）
        // 如果 TripDetails 的 pickupPoints 字段定义为 String，存 JSON 也是可以的。
        // 但前端显示可能依赖于这个字段的格式。
        // 假设 TripDetails 仍然存储显示的名称字符串（为了兼容旧逻辑），或者我们可以存 JSON。
        // 鉴于 TripDetails 是历史记录，存 JSON 更好。但为了不破坏现有显示逻辑（如果前端直接显示这个字段），
        // 我们先保持存名称字符串，或者确认前端如何使用。
        // 实际上，TripDetails 的 pickupPoints 字段在前端展示时，如果是 JSON 字符串，前端可能需要解析。
        // 暂时保持存名称字符串给 TripDetails，但更新 UserCommuteConfig 时使用 JSON。

        details.setPickupPoints(pickupPoints);
        details.setDropoffPoints(dropoffPoints);

        // 价格明细
        details.setPricePerSeat(priceFen);
        details.setSeatCount(seatCount);
        details.setTotalIncome(MoneyUtils.calculateTotal(priceFen, seatCount));

        // 行程配置
        String notes = (String) tripData.get("notes");
        if (notes == null && config.getDefaultNotes() != null) {
            notes = config.getDefaultNotes();
        }
        details.setNotes(notes);

        Boolean recurring = tripData.get("recurring") != null ? (Boolean) tripData.get("recurring") : false;
        config.setDefaultRecurring(recurring);

        String recurringType = (String) tripData.get("recurringType");
        if (recurringType == null && config.getDefaultRecurringType() != null) {
            recurringType = config.getDefaultRecurringType();
        }
        config.setDefaultRecurringType(recurringType);

        // 5. 保存trip_details
        tripDetailsMapper.insertTripDetails(details);

        // 6. 更新user_commute_config（保存本次配置供下次使用）
        config.setDefaultSeatCount(seatCount);
        config.setDefaultPricePerSeat(priceFen);
        config.setDefaultNotes(notes);
        config.setDefaultRecurringType(recurringType);

        // 优先使用 JSON 更新配置
        if (pickupPointsJson != null && !pickupPointsJson.isEmpty()) {
            config.setPickupPoints(pickupPointsJson);
        } else if (config.getPickupPoints() == null) {
            // 只有在没有配置且没有JSON时，才使用简单字符串（兼容旧数据）
            config.setPickupPoints(pickupPoints);
        }

        if (dropoffPointsJson != null && !dropoffPointsJson.isEmpty()) {
            config.setDropoffPoints(dropoffPointsJson);
        } else if (config.getDropoffPoints() == null) {
            config.setDropoffPoints(dropoffPoints);
        }

        userCommuteConfigMapper.insertOrUpdate(config);

        logger.info("司机行程发布成功: tripId={}, userId={}, startLocation={}, endLocation={}, price={}分",
                tripId, userId, tripData.get("startLocation"), tripData.get("endLocation"), priceFen);

        return tripId;
    }

    @Override
    public String publishPassengerTrip(Map<String, Object> tripData) {
        String userId = (String) tripData.get("userId");
        String timing = (String) tripData.get("timing");
        String tripId = IdGeneratorUtils.generateId();
        String detailsId = IdGeneratorUtils.generateId();

        // 1. 从user_commute_config读取默认配置（如果存在）
        UserCommuteConfig config = userCommuteConfigMapper.findByUserIdAndTiming(userId, timing);
        if (config == null) {
            config = new UserCommuteConfig();
            config.setId(IdGeneratorUtils.generateId());
            config.setUserId(userId);
            config.setTiming(timing);
        }

        // 2. 构建Trip核心数据（精简版）
        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUserId(userId); // 乘客发布的行程，这里存储乘客ID
        trip.setType("people_seeking_car");

        // 设置起终点信息
        trip.setStartLocation((String) tripData.get("startLocation"));
        trip.setStartCity((String) tripData.get("startCity"));
        trip.setEndLocation((String) tripData.get("endLocation"));
        trip.setEndCity((String) tripData.get("endCity"));

        // 设置经纬度
        if (tripData.get("startLongitude") != null) {
            trip.setStartLongitude(Double.parseDouble(tripData.get("startLongitude").toString()));
        }
        if (tripData.get("startLatitude") != null) {
            trip.setStartLatitude(Double.parseDouble(tripData.get("startLatitude").toString()));
        }
        if (tripData.get("endLongitude") != null) {
            trip.setEndLongitude(Double.parseDouble(tripData.get("endLongitude").toString()));
        }
        if (tripData.get("endLatitude") != null) {
            trip.setEndLatitude(Double.parseDouble(tripData.get("endLatitude").toString()));
        }

        // 设置出发时间
        trip.setDepartureTime(LocalDateTime.parse((String) tripData.get("departureTime")));
        trip.setTiming(timing);

        // 乘客行程不设置座位数，设置为null
        trip.setAvailableSeats(null);
        trip.setBookedSeats(0);

        // 金额转换：前端传来元，转为分存储（乘客出价）
        Long priceFen = null;
        if (tripData.get("cost") != null) {
            Double priceYuan = Double.parseDouble(tripData.get("cost").toString());
            priceFen = MoneyUtils.yuanToFen(priceYuan);
        } else if (config.getDefaultOfferPrice() != null) {
            priceFen = config.getDefaultOfferPrice();
        } else {
            priceFen = 2000L; // 默认20元
        }
        trip.setPrice(priceFen);

        // 设置状态和时间戳
        trip.setStatus("available");
        // 设置状态和时间戳
        trip.setStatus("available");

        // 3. 保存trips核心数据
        tripMapper.insertTrip(trip);

        // 4. 构建TripDetails扩展数据
        TripDetails details = new TripDetails();
        details.setId(detailsId);
        details.setTripId(tripId);

        // 乘客信息
        Integer passengerCount = tripData.get("passengerCount") != null
                ? Integer.parseInt(tripData.get("passengerCount").toString())
                : (config.getDefaultPassengerCount() != null ? config.getDefaultPassengerCount() : 1);
        details.setPassengerCount(passengerCount);

        // 价格（乘客总出价）
        details.setBasePrice(priceFen);

        // 行程配置
        String notes = (String) tripData.get("notes");
        if (notes == null && config.getDefaultNotes() != null) {
            notes = config.getDefaultNotes();
        }
        details.setNotes(notes);

        // 时间戳

        // 5. 保存trip_details
        tripDetailsMapper.insertTripDetails(details);

        // 6. 更新user_commute_config（保存本次配置供下次使用）
        config.setDefaultPassengerCount(passengerCount);
        config.setDefaultOfferPrice(priceFen);
        config.setDefaultNotes(notes);

        userCommuteConfigMapper.insertOrUpdate(config);

        logger.info("乘客行程发布成功: tripId={}, userId={}, startLocation={}, endLocation={}, offerPrice={}分",
                tripId, userId, tripData.get("startLocation"), tripData.get("endLocation"), priceFen);

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
            // 更新行程状态
            trip.setStatus("cancelled");
            tripMapper.updateTrip(trip);

            // 更新trip_details中的备注
            TripDetails details = tripDetailsMapper.selectByTripId(tripId);
            if (details != null) {
                String originalNotes = details.getNotes() != null ? details.getNotes() : "";
                details.setNotes(originalNotes + " [取消原因: " + reason + "]");
                tripDetailsMapper.updateTripDetails(details);
            }

            return true;
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

        // 从用户设置中获取家和公司的坐标
        final String currentUserId = (String) params.get("currentUserId");
        final Double[] homeCoords = new Double[2]; // [0]=lat, [1]=lng
        final Double[] companyCoords = new Double[2]; // [0]=lat, [1]=lng

        if (currentUserId != null) {
            try {
                // 查询用户的家位置
                UserLocation homeLocation = userLocationMapper.findByUserIdAndType(currentUserId, "home");
                if (homeLocation != null && homeLocation.getLatitude() != null && homeLocation.getLongitude() != null) {
                    homeCoords[0] = homeLocation.getLatitude();
                    homeCoords[1] = homeLocation.getLongitude();
                    logger.info("用户 {} 的家位置: {}, 坐标({}, {})",
                            currentUserId, homeLocation.getName(), homeCoords[0], homeCoords[1]);
                } else {
                    logger.warn("用户 {} 未设置家位置", currentUserId);
                }

                // 查询用户的公司位置
                UserLocation companyLocation = userLocationMapper.findByUserIdAndType(currentUserId, "company");
                if (companyLocation != null && companyLocation.getLatitude() != null
                        && companyLocation.getLongitude() != null) {
                    companyCoords[0] = companyLocation.getLatitude();
                    companyCoords[1] = companyLocation.getLongitude();
                    logger.info("用户 {} 的公司位置: {}, 坐标({}, {})",
                            currentUserId, companyLocation.getName(), companyCoords[0], companyCoords[1]);
                } else {
                    logger.warn("用户 {} 未设置公司位置", currentUserId);
                }
            } catch (Exception e) {
                logger.warn("获取用户位置信息失败: userId={}, error={}", currentUserId, e.getMessage());
            }
        }

        // 获取已关注的用户列表（如果需要）
        Set<String> followedUserIds = null;
        Object followsOnlyObj = params.get("followsOnly");
        boolean followsOnly = false;
        if (followsOnlyObj != null) {
            followsOnly = Boolean.parseBoolean(followsOnlyObj + "");
        }
        if (followsOnly) {
            followedUserIds = getFollowedUserIds(currentUserId);
        }

        final Set<String> finalFollowedUserIds = followedUserIds;
        final boolean finalFollowsOnly = followsOnly;
        List<Trip> filteredTrips = trips.stream()
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

                    // 家附近距离筛选（只检查起点距离）
                    // 只有当用户设置了家位置时才进行距离筛选
                    Object homeDistanceObj = params.get("homeDistance");
                    if (homeDistanceObj != null) {
                        if (homeCoords[0] == null || homeCoords[1] == null) {
                            // 用户未设置家位置，跳过距离筛选（不过滤）
                            logger.info("家附近距离筛选: 用户未设置家位置，跳过筛选");
                        } else {
                            try {
                                int homeDistance = homeDistanceObj instanceof String
                                        ? Integer.parseInt((String) homeDistanceObj)
                                        : (Integer) homeDistanceObj;
                                if (homeDistance > 0 && trip.getStartLatitude() != null
                                        && trip.getStartLongitude() != null) {
                                    double distance = calculateDistance(homeCoords[0], homeCoords[1],
                                            trip.getStartLatitude(), trip.getStartLongitude());
                                    if (distance > homeDistance) {
                                        logger.info("家附近距离筛选: 行程 {} 被过滤 - 起点:{} 距离家{}米, 超过{}米",
                                                trip.getId(), trip.getStartLocation(), (int) distance, homeDistance);
                                        return false;
                                    } else {
                                        logger.info("家附近距离筛选: 行程 {} 通过 - 起点:{} 距离家{}米",
                                                trip.getId(), trip.getStartLocation(), (int) distance);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的距离参数
                            }
                        }
                    }

                    // 公司附近距离筛选（只检查起点距离）
                    // 只有当用户设置了公司位置时才进行距离筛选
                    Object companyDistanceObj = params.get("companyDistance");
                    if (companyDistanceObj != null) {
                        if (companyCoords[0] == null || companyCoords[1] == null) {
                            // 用户未设置公司位置，跳过距离筛选（不过滤）
                            logger.info("公司附近距离筛选: 用户未设置公司位置，跳过筛选");
                        } else {
                            try {
                                int companyDistance = companyDistanceObj instanceof String
                                        ? Integer.parseInt((String) companyDistanceObj)
                                        : (Integer) companyDistanceObj;
                                if (companyDistance > 0 && trip.getStartLatitude() != null
                                        && trip.getStartLongitude() != null) {
                                    double distance = calculateDistance(companyCoords[0], companyCoords[1],
                                            trip.getStartLatitude(), trip.getStartLongitude());
                                    if (distance > companyDistance) {
                                        logger.info("公司附近距离筛选: 行程 {} 被过滤 - 起点:{} 距离公司{}米, 超过{}米",
                                                trip.getId(), trip.getStartLocation(), (int) distance, companyDistance);
                                        return false;
                                    } else {
                                        logger.info("公司附近距离筛选: 行程 {} 通过 - 起点:{} 距离公司{}米",
                                                trip.getId(), trip.getStartLocation(), (int) distance);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的距离参数
                            }
                        }
                    }

                    // 关注筛选
                    if (finalFollowsOnly &&
                            (finalFollowedUserIds == null || !finalFollowedUserIds.contains(trip.getUserId()))) {
                        return false;
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
                            logger.debug("地铁站筛选: 行程 {} 被过滤 - 起点:{}, 终点:{}, 需要包含地铁站:{}",
                                    trip.getId(), startLocation, endLocation, subwayStations);
                            return false;
                        }
                    }

                    return true;
                })
                .collect(ArrayList::new, (list, trip) -> list.add(trip), ArrayList::addAll);

        // 填充用户信息（司机姓名和车辆信息）
        filteredTrips.forEach(trip -> {
            try {
                User user = userMapper.findById(trip.getUserId());
                if (user != null) {
                    trip.setDriverName(user.getName());
                    // 组合车辆信息：品牌+颜色
                    if (user.getVehicleBrand() != null || user.getVehicleColor() != null) {
                        StringBuilder carInfo = new StringBuilder();
                        if (user.getVehicleBrand() != null) {
                            carInfo.append(user.getVehicleBrand());
                        }
                        if (user.getVehicleColor() != null) {
                            if (carInfo.length() > 0) {
                                carInfo.append(" ");
                            }
                            carInfo.append(user.getVehicleColor());
                        }
                        trip.setCarInfo(carInfo.toString());
                    }
                }
            } catch (Exception e) {
                logger.warn("填充行程用户信息失败: tripId={}, userId={}, error={}",
                        trip.getId(), trip.getUserId(), e.getMessage());
            }
        });

        return filteredTrips;
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
     * 
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
            case "yonganli":
                return "永安里";
            case "jianguomen":
                return "建国门";
            case "beijingzhan":
                return "北京站";
            case "guomao":
                return "国贸";
            case "dongdaqiao":
                return "东大桥";
            case "jintaixizhao":
                return "金台夕照";
            case "chaoyangmen":
                return "朝阳门";
            case "guangmenmenwai":
                return "广渠门外";
            default:
                return null;
        }
    }
}