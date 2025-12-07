package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    // 模拟用户存储
    private Map<String, User> userStorage = new HashMap<>();

    public UserServiceImpl() {
        initSampleData();
    }

    @Override
    public User getUserById(String userId) {
        return userStorage.computeIfAbsent(userId, k -> {
            User user = new User();
            user.setId(userId);
            user.setName("用户" + userId.substring(userId.length() - 3));
            user.setPhone("138****8888");
            user.setAvatar("/static/default-avatar.png");
            user.setBalance(new BigDecimal("0"));
            user.setCoupons(2);
            user.setHistoryOrders(15);
            user.setVerificationStatus("none");
            user.setCreateTime(LocalDateTime.now());
            return user;
        });
    }

    @Override
    public boolean updateUser(User user) {
        User existingUser = userStorage.get(user.getId());
        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setPhone(user.getPhone());
            existingUser.setAvatar(user.getAvatar());
            existingUser.setCommunity(user.getCommunity());
            existingUser.setVehicleBrand(user.getVehicleBrand());
            existingUser.setVehicleColor(user.getVehicleColor());
            existingUser.setPlateNumber(user.getPlateNumber());
            existingUser.setUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getUserLocations(String userId) {
        List<Map<String, Object>> locations = new ArrayList<>();
        
        Map<String, Object> location1 = new HashMap<>();
        location1.put("id", "loc_001");
        location1.put("name", "荣盛阿尔卡迪亚");
        location1.put("address", "廊坊市·荣盛阿尔卡迪亚·花语城七地块");
        location1.put("longitude", 116.7);
        location1.put("latitude", 39.5);
        location1.put("type", "home");
        locations.add(location1);

        Map<String, Object> location2 = new HashMap<>();
        location2.put("id", "loc_002");
        location2.put("name", "建国门");
        location2.put("address", "北京市东城区建国门");
        location2.put("longitude", 116.4);
        location2.put("latitude", 39.9);
        location2.put("type", "company");
        locations.add(location2);

        return locations;
    }

    @Override
    public String saveUserLocation(Map<String, Object> location) {
        String locationId = "loc_" + UUID.randomUUID().toString().substring(0, 8);
        location.put("id", locationId);
        // 这里应该保存到数据库
        return locationId;
    }

    @Override
    public List<Map<String, Object>> getFollowedDrivers(String userId) {
        List<Map<String, Object>> drivers = new ArrayList<>();
        
        Map<String, Object> driver1 = new HashMap<>();
        driver1.put("id", "driver_001");
        driver1.put("name", "张师傅");
        driver1.put("avatar", "/static/driver1.png");
        driver1.put("location", "荣盛阿尔卡迪亚");
        driver1.put("vehicleInfo", "白色 特斯拉 Model 3");
        driver1.put("followTime", LocalDateTime.now().minusDays(5).toString());
        drivers.add(driver1);

        return drivers;
    }

    @Override
    public boolean followDriver(String userId, String driverId) {
        // 模拟关注操作
        return true;
    }

    @Override
    public boolean unfollowDriver(String userId, String driverId) {
        // 模拟取消关注操作
        return true;
    }

    @Override
    public List<Map<String, Object>> getBlacklistedUsers(String userId) {
        List<Map<String, Object>> blacklistedUsers = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "user_999");
        user1.put("name", "不良用户");
        user1.put("avatar", "/static/user-avatar.png");
        user1.put("location", "某地");
        user1.put("blacklistTime", LocalDateTime.now().minusDays(1).toString());
        user1.put("reason", "行为不当");
        blacklistedUsers.add(user1);

        return blacklistedUsers;
    }

    @Override
    public boolean addToBlacklist(String userId, String targetUserId, String reason) {
        // 模拟添加黑名单操作
        return true;
    }

    @Override
    public boolean removeFromBlacklist(String userId, String targetUserId) {
        // 模拟移除黑名单操作
        return true;
    }

    @Override
    public boolean submitDriverVerification(Map<String, Object> verificationData) {
        // 模拟提交认证操作
        String userId = (String) verificationData.get("userId");
        User user = userStorage.get(userId);
        if (user != null) {
            user.setVerificationStatus("pending");
            user.setUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public String uploadAvatar(String userId, MultipartFile file) {
        // 模拟文件上传
        String filename = userId + "_avatar_" + System.currentTimeMillis() + ".jpg";
        String avatarUrl = "/uploads/avatars/" + filename;
        
        User user = userStorage.get(userId);
        if (user != null) {
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
        }
        
        return avatarUrl;
    }

    @Override
    public String uploadVerificationFile(String userId, MultipartFile file, String fileType) {
        // 模拟认证文件上传
        String filename = userId + "_" + fileType + "_" + System.currentTimeMillis() + ".jpg";
        return "/uploads/verification/" + filename;
    }

    private void initSampleData() {
        // 初始化示例用户数据
        User sampleUser = new User();
        sampleUser.setId("user_001");
        sampleUser.setName("张三");
        sampleUser.setPhone("13812345678");
        sampleUser.setAvatar("/static/user-avatar.png");
        sampleUser.setCommunity("荣盛阿尔卡迪亚");
        sampleUser.setBalance(new BigDecimal("150.50"));
        sampleUser.setCoupons(3);
        sampleUser.setHistoryOrders(25);
        sampleUser.setVerificationStatus("verified");
        sampleUser.setCreateTime(LocalDateTime.now().minusMonths(2));
        userStorage.put("user_001", sampleUser);
    }
}