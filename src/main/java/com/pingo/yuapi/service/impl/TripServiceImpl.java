package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Trip;
import com.pingo.yuapi.entity.TripDetails;
import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.entity.UserCommuteConfig;
import com.pingo.yuapi.entity.UserLocation;
import com.pingo.yuapi.entity.Vehicle;
import com.pingo.yuapi.mapper.TripMapper;
import com.pingo.yuapi.mapper.TripDetailsMapper;
import com.pingo.yuapi.mapper.UserMapper;
import com.pingo.yuapi.mapper.UserCommuteConfigMapper;
import com.pingo.yuapi.mapper.UserLocationMapper;
import com.pingo.yuapi.mapper.VehicleMapper;
import com.pingo.yuapi.service.TripService;
import com.pingo.yuapi.utils.DateUtils;
import com.pingo.yuapi.utils.GsonUtils;
import com.pingo.yuapi.utils.IdGeneratorUtils;
import com.pingo.yuapi.utils.MoneyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
    private VehicleMapper vehicleMapper;

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
        Trip trip = tripMapper.selectTripById(tripId);
        if (trip != null) {
            // 填充途经点信息、备注和车辆信息
            TripDetails details = tripDetailsMapper.selectByTripId(tripId);
            if (details != null) {
                trip.setPickupPoints(parseWaypoints(details.getPickupPoints()));
                trip.setDropoffPoints(parseWaypoints(details.getDropoffPoints()));
                trip.setNotes(details.getNotes());

                // 🔧 优先从 TripDetails 读取司机和车辆信息（历史快照）
                if (details.getDriverName() != null) {
                    trip.setDriverName(details.getDriverName());
                }
                if (details.getDriverAvatar() != null) {
                    trip.setDriverAvatar(details.getDriverAvatar());
                }
                if (details.getVehicleInfo() != null) {
                    trip.setCarInfo(details.getVehicleInfo());
                }
                if (details.getPlateNumber() != null) {
                    trip.setPlateNumber(details.getPlateNumber());
                }
            }

            // 如果 TripDetails 中没有司机或车辆信息，从 User 表查询（兼容旧数据）
            if (trip.getDriverName() == null || trip.getDriverAvatar() == null ||
                    trip.getCarInfo() == null || trip.getPlateNumber() == null) {
                User user = userMapper.findById(trip.getUserId());
                if (user != null) {
                    if (trip.getDriverName() == null) {
                        trip.setDriverName(user.getName());
                    }
                    if (trip.getDriverAvatar() == null) {
                        trip.setDriverAvatar(user.getAvatar());
                    }

                    // 组合车辆信息：颜色+品牌
                    if (trip.getCarInfo() == null
                            && (user.getVehicleBrand() != null || user.getVehicleColor() != null)) {
                        StringBuilder carInfo = new StringBuilder();
                        if (user.getVehicleColor() != null) {
                            carInfo.append(user.getVehicleColor());
                        }
                        if (user.getVehicleBrand() != null) {
                            if (carInfo.length() > 0) {
                                carInfo.append(" ");
                            }
                            carInfo.append(user.getVehicleBrand());
                        }
                        trip.setCarInfo(carInfo.toString());
                    }

                    if (trip.getPlateNumber() == null) {
                        trip.setPlateNumber(user.getPlateNumber());
                    }
                }
            }
        }
        return trip;
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
        String pickupPoints = tripData.get("pickupPoint") + "";
        String dropoffPoints = tripData.get("dropoffPoint") + "";

        // 获取JSON格式的途经点（包含经纬度）
        String pickupPointsJson = tripData.get("pickupPointsJson") + "";
        String dropoffPointsJson = tripData.get("dropoffPointsJson") + "";

        if (!StringUtils.hasText(pickupPoints) && config.getPickupPoints() != null) {
            pickupPoints = config.getPickupPoints();
        }
        if (!StringUtils.hasText(dropoffPoints) && config.getDropoffPoints() != null) {
            dropoffPoints = config.getDropoffPoints();
        }

        // 🔧 优先保存JSON格式的途经点数据（包含经纬度）到trip_details
        // 如果前端传了JSON数据，优先使用JSON；否则使用简单字符串（兼容旧数据）
        if (StringUtils.hasText(pickupPointsJson)) {
            details.setPickupPoints(pickupPointsJson);
        } else if (StringUtils.hasText(pickupPoints)) {
            details.setPickupPoints(pickupPoints);
        } else if (StringUtils.hasText(config.getPickupPoints())) {
            details.setPickupPoints(config.getPickupPoints());
        }

        if (StringUtils.hasText(dropoffPointsJson)) {
            details.setDropoffPoints(dropoffPointsJson);
        } else if (StringUtils.hasText(dropoffPoints)) {
            details.setDropoffPoints(dropoffPoints);
        } else if (StringUtils.hasText(config.getDropoffPoints())) {
            details.setDropoffPoints(config.getDropoffPoints());
        }

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

        // 🔧 查询用户信息并填充到 trip_details（车辆信息快照）
        User user = userMapper.findById(userId);
        if (user != null) {
            // 填充司机基本信息
            details.setDriverName(user.getName());
            details.setDriverAvatar(user.getAvatar());
            details.setDriverPhone(user.getPhone());
        }

        // 🔧 查询车辆信息并填充到 trip_details（车辆信息快照）
        // 优先从 vehicles 表查询，如果没有则从 users 表查询（兼容旧数据）
        Vehicle vehicle = vehicleMapper.findDefaultByUserId(userId);
        if (vehicle != null) {
            // 保存车辆ID引用
            details.setVehicleId(vehicle.getId());

            // 填充车牌号
            details.setPlateNumber(vehicle.getPlateNumber());

            // 组合车辆信息：颜色+品牌
            if (vehicle.getBrand() != null || vehicle.getColor() != null) {
                StringBuilder vehicleInfo = new StringBuilder();
                if (vehicle.getColor() != null) {
                    vehicleInfo.append(vehicle.getColor());
                }
                if (vehicle.getBrand() != null) {
                    if (vehicleInfo.length() > 0) {
                        vehicleInfo.append(" ");
                    }
                    vehicleInfo.append(vehicle.getBrand());
                }
                details.setVehicleInfo(vehicleInfo.toString());
            }

            logger.info("填充车辆信息（从vehicles表）: userId={}, vehicleId={}, plateNumber={}, vehicleInfo={}",
                    userId, vehicle.getId(), vehicle.getPlateNumber(), details.getVehicleInfo());
        } else if (user != null) {
            // 兼容旧数据：如果 vehicles 表中没有数据，从 users 表查询
            details.setPlateNumber(user.getPlateNumber());

            // 组合车辆信息：品牌+颜色
            if (user.getVehicleBrand() != null || user.getVehicleColor() != null) {
                StringBuilder vehicleInfo = new StringBuilder();
                if (user.getVehicleColor() != null) {
                    vehicleInfo.append(user.getVehicleColor());
                }
                if (user.getVehicleBrand() != null) {
                    if (vehicleInfo.length() > 0) {
                        vehicleInfo.append(" ");
                    }
                    vehicleInfo.append(user.getVehicleBrand());
                }
                details.setVehicleInfo(vehicleInfo.toString());
            }

            logger.info("填充车辆信息（从users表）: userId={}, plateNumber={}, vehicleInfo={}",
                    userId, user.getPlateNumber(), details.getVehicleInfo());
        }

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

        // 预加载所有行程的TripDetails（用于检查途经点）
        final Map<String, TripDetails> tripDetailsMap = new HashMap<>();
        for (Trip trip : trips) {
            try {
                TripDetails details = tripDetailsMapper.selectByTripId(trip.getId());
                if (details != null) {
                    tripDetailsMap.put(trip.getId(), details);
                }
            } catch (Exception e) {
                logger.warn("加载行程详情失败: tripId={}, error={}", trip.getId(), e.getMessage());
            }
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

                    // 家附近距离筛选（检查所有途经点）
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
                                if (homeDistance > 0) {
                                    // 获取行程的途经点详情
                                    TripDetails details = tripDetailsMap.get(trip.getId());
                                    // 检查途经点中是否有符合距离条件的（包含回退到起点/终点的逻辑）
                                    boolean hasNearbyWaypoint = hasWaypointWithinDistance(trip, details, homeCoords[0],
                                            homeCoords[1], homeDistance);
                                    if (!hasNearbyWaypoint) {
                                        logger.info("家附近距离筛选: 行程 {} 被过滤 - 没有途经点在家附近{}米范围内",
                                                trip.getId(), homeDistance);
                                        return false;
                                    } else {
                                        logger.info("家附近距离筛选: 行程 {} 通过 - 有途经点在家附近", trip.getId());
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的距离参数
                            }
                        }
                    }

                    // 公司附近距离筛选（检查所有途经点）
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
                                if (companyDistance > 0) {
                                    // 获取行程的途经点详情
                                    TripDetails details = tripDetailsMap.get(trip.getId());
                                    // 检查途经点中是否有符合距离条件的（包含回退到起点/终点的逻辑）
                                    boolean hasNearbyWaypoint = hasWaypointWithinDistance(trip, details,
                                            companyCoords[0],
                                            companyCoords[1], companyDistance);
                                    if (!hasNearbyWaypoint) {
                                        logger.info("公司附近距离筛选: 行程 {} 被过滤 - 没有途经点在公司附近{}米范围内",
                                                trip.getId(), companyDistance);
                                        return false;
                                    } else {
                                        logger.info("公司附近距离筛选: 行程 {} 通过 - 有途经点在公司附近", trip.getId());
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

                    // 地铁站筛选（检查所有途经点）
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
                        TripDetails details = tripDetailsMap.get(trip.getId());

                        for (String stationId : subwayStations) {
                            String stationName = getSubwayStationName(stationId);
                            if (stationName != null) {
                                // 检查途经点中是否包含该地铁站
                                if (hasWaypointWithSubwayStation(details, stationName)) {
                                    foundMatch = true;
                                    break;
                                }
                            }
                        }

                        if (!foundMatch) {
                            logger.debug("地铁站筛选: 行程 {} 被过滤 - 没有途经点包含地铁站:{}",
                                    trip.getId(), subwayStations);
                            return false;
                        } else {
                            logger.info("地铁站筛选: 行程 {} 通过 - 途经点包含地铁站", trip.getId());
                        }
                    }

                    return true;
                })
                .collect(ArrayList::new, (list, trip) -> list.add(trip), ArrayList::addAll);

        // 填充用户信息（司机姓名、车辆信息、车牌号）和途经点信息、备注
        filteredTrips.forEach(trip -> {
            try {
                // 填充途经点信息和备注
                TripDetails details = tripDetailsMap.get(trip.getId());
                if (details != null) {
                    trip.setPickupPoints(parseWaypoints(details.getPickupPoints()));
                    trip.setDropoffPoints(parseWaypoints(details.getDropoffPoints()));
                    // 🔧 填充备注
                    trip.setNotes(details.getNotes());

                    // 🔧 优先从 TripDetails 读取司机和车辆信息（历史快照）
                    if (details.getDriverName() != null) {
                        trip.setDriverName(details.getDriverName());
                    }
                    if (details.getDriverAvatar() != null) {
                        trip.setDriverAvatar(details.getDriverAvatar());
                    }
                    if (details.getVehicleInfo() != null) {
                        trip.setCarInfo(details.getVehicleInfo());
                    }
                    if (details.getPlateNumber() != null) {
                        trip.setPlateNumber(details.getPlateNumber());
                    }
                }

                // 如果 TripDetails 中没有司机或车辆信息，从 User 表查询（兼容旧数据）
                if (trip.getDriverName() == null || trip.getDriverAvatar() == null ||
                        trip.getCarInfo() == null || trip.getPlateNumber() == null) {
                    User user = userMapper.findById(trip.getUserId());
                    if (user != null) {
                        if (trip.getDriverName() == null) {
                            trip.setDriverName(user.getName());
                        }
                        if (trip.getDriverAvatar() == null) {
                            trip.setDriverAvatar(user.getAvatar());
                        }

                        // 组合车辆信息：颜色+品牌
                        if (trip.getCarInfo() == null
                                && (user.getVehicleBrand() != null || user.getVehicleColor() != null)) {
                            StringBuilder carInfo = new StringBuilder();
                            if (user.getVehicleColor() != null) {
                                carInfo.append(user.getVehicleColor());
                            }
                            if (user.getVehicleBrand() != null) {
                                if (carInfo.length() > 0) {
                                    carInfo.append(" ");
                                }
                                carInfo.append(user.getVehicleBrand());
                            }
                            trip.setCarInfo(carInfo.toString());
                        }

                        if (trip.getPlateNumber() == null) {
                            trip.setPlateNumber(user.getPlateNumber());
                        }
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

    /**
     * 途经点类，用于解析JSON
     */
    private static class Waypoint {
        private String name;
        private Double longitude;
        private Double latitude;
        private String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    /**
     * 解析途经点JSON字符串
     * 兼容两种格式：
     * 1. JSON数组格式（包含经纬度）: [{"name":"地点","longitude":116.0,"latitude":39.0}]
     * 2. 简单字符串格式（仅名称）: "地点1、地点2、地点3"
     */
    private List<Waypoint> parseWaypoints(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 尝试JSON格式
        if (jsonStr.trim().startsWith("[")) {
            try {
                return GsonUtils.fromJson2List(jsonStr, Waypoint.class);
            } catch (Exception e) {
                logger.debug("解析途经点JSON失败: {}, error={}", jsonStr, e.getMessage());
            }
        }

        // 简单字符串格式（仅名称，无坐标）
        // 这种格式无法用于距离过滤，但可以用于地铁站过滤
        String[] names = jsonStr.split("[、,]");
        List<Waypoint> waypoints = new ArrayList<>();
        for (String name : names) {
            if (name != null && !name.trim().isEmpty()) {
                Waypoint wp = new Waypoint();
                wp.setName(name.trim());
                waypoints.add(wp);
            }
        }
        return waypoints;
    }

    /**
     * 检查行程的任意途经点（包括起点、终点、上车点、下车点）是否在指定距离内
     * 只要有任意一个点符合距离条件就返回true
     *
     * @param trip        行程对象（用于获取起点/终点坐标）
     * @param tripDetails 行程详情
     * @param targetLat   目标纬度
     * @param targetLng   目标经度
     * @param maxDistance 最大距离（米）
     * @return 是否有途经点在范围内
     */
    private boolean hasWaypointWithinDistance(Trip trip, TripDetails tripDetails, double targetLat, double targetLng,
            int maxDistance) {

        // 检查途经点（如果有坐标信息）
        if (tripDetails != null) {
            // 检查上车点
            List<Waypoint> pickupPoints = parseWaypoints(tripDetails.getPickupPoints());
            for (Waypoint point : pickupPoints) {
                if (point.getLatitude() != null && point.getLongitude() != null) {
                    double distance = calculateDistance(targetLat, targetLng, point.getLatitude(),
                            point.getLongitude());
                    if (distance <= maxDistance) {
                        logger.info("途经点符合距离: 上车点 {} 距离{}米", point.getName(), (int) distance);
                        return true;
                    }
                }
            }

            // 检查下车点
            List<Waypoint> dropoffPoints = parseWaypoints(tripDetails.getDropoffPoints());
            for (Waypoint point : dropoffPoints) {
                if (point.getLatitude() != null && point.getLongitude() != null) {
                    double distance = calculateDistance(targetLat, targetLng, point.getLatitude(),
                            point.getLongitude());
                    if (distance <= maxDistance) {
                        logger.info("途经点符合距离: 下车点 {} 距离{}米", point.getName(), (int) distance);
                        return true;
                    }
                }
            }
        }

        // // 始终检查起点和终点坐标（即使途经点有坐标也要检查）
        // if (trip != null) {
        // // 检查起点
        // if (trip.getStartLatitude() != null && trip.getStartLongitude() != null) {
        // double distance = calculateDistance(targetLat, targetLng,
        // trip.getStartLatitude(),
        // trip.getStartLongitude());
        // if (distance <= maxDistance) {
        // logger.info("起点符合距离: {} 距离{}米", trip.getStartLocation(), (int) distance);
        // return true;
        // }
        // }

        // // 检查终点
        // if (trip.getEndLatitude() != null && trip.getEndLongitude() != null) {
        // double distance = calculateDistance(targetLat, targetLng,
        // trip.getEndLatitude(),
        // trip.getEndLongitude());
        // if (distance <= maxDistance) {
        // logger.info("终点符合距离: {} 距离{}米", trip.getEndLocation(), (int) distance);
        // return true;
        // }
        // }
        // }

        return false;
    }

    /**
     * 检查行程的任意途经点是否包含指定地铁站
     * 
     * @param tripDetails 行程详情
     * @param stationName 地铁站名称
     * @return 是否有途经点包含该地铁站
     */
    private boolean hasWaypointWithSubwayStation(TripDetails tripDetails, String stationName) {
        if (tripDetails == null || stationName == null) {
            return false;
        }

        // 检查上车点
        List<Waypoint> pickupPoints = parseWaypoints(tripDetails.getPickupPoints());
        for (Waypoint point : pickupPoints) {
            if (point.getName() != null && point.getName().contains(stationName)) {
                logger.info("途经点包含地铁站: 上车点 {} 包含 {}", point.getName(), stationName);
                return true;
            }
        }

        // 检查下车点
        List<Waypoint> dropoffPoints = parseWaypoints(tripDetails.getDropoffPoints());
        for (Waypoint point : dropoffPoints) {
            if (point.getName() != null && point.getName().contains(stationName)) {
                logger.info("途经点包含地铁站: 下车点 {} 包含 {}", point.getName(), stationName);
                return true;
            }
        }

        return false;
    }
}