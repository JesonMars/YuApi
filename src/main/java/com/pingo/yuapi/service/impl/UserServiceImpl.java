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

    @Override
    public boolean updateNickname(String userId, String nickname) {
        User user = userStorage.get(userId);
        if (user != null) {
            user.setName(nickname);
            user.setUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getUserStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        User user = userStorage.get(userId);
        
        if (user != null) {
            stats.put("trips", user.getHistoryOrders());
            stats.put("savedMoney", 1580.5);
            stats.put("carbon", 45.2);
        } else {
            stats.put("trips", 0);
            stats.put("savedMoney", 0.0);
            stats.put("carbon", 0.0);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserSettings(String userId) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("messageNotification", true);
        settings.put("tripReminder", true);
        settings.put("soundEnabled", true);
        settings.put("vibrationEnabled", false);
        settings.put("nightMode", false);
        return settings;
    }

    @Override
    public boolean updateUserSettings(String userId, Map<String, Object> settings) {
        // 模拟更新用户设置
        // 在实际应用中，这里会保存设置到数据库
        return true;
    }

    @Override
    public Map<String, Object> getUserWallet(String userId) {
        User user = getUserById(userId);
        Map<String, Object> walletInfo = new HashMap<>();
        
        walletInfo.put("balance", user.getBalance());
        walletInfo.put("frozenAmount", new BigDecimal("0.00"));
        walletInfo.put("coupons", user.getCoupons());
        walletInfo.put("points", 1250);
        
        return walletInfo;
    }

    @Override
    public boolean rechargeWallet(String userId, Double amount, String paymentMethod) {
        User user = userStorage.get(userId);
        if (user != null) {
            BigDecimal currentBalance = user.getBalance();
            BigDecimal newBalance = currentBalance.add(new BigDecimal(amount.toString()));
            user.setBalance(newBalance);
            user.setUpdateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public boolean withdrawWallet(String userId, Double amount, String bankAccount) {
        User user = userStorage.get(userId);
        if (user != null) {
            BigDecimal currentBalance = user.getBalance();
            BigDecimal withdrawAmount = new BigDecimal(amount.toString());
            
            if (currentBalance.compareTo(withdrawAmount) >= 0) {
                BigDecimal newBalance = currentBalance.subtract(withdrawAmount);
                user.setBalance(newBalance);
                user.setUpdateTime(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getWalletTransactions(String userId, Integer page, Integer limit) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        
        // 模拟交易记录
        Map<String, Object> transaction1 = new HashMap<>();
        transaction1.put("id", "txn_001");
        transaction1.put("type", "recharge");
        transaction1.put("amount", 100.00);
        transaction1.put("description", "微信充值");
        transaction1.put("createTime", LocalDateTime.now().minusDays(1).toString());
        transaction1.put("status", "success");
        transactions.add(transaction1);
        
        Map<String, Object> transaction2 = new HashMap<>();
        transaction2.put("id", "txn_002");
        transaction2.put("type", "consume");
        transaction2.put("amount", -15.50);
        transaction2.put("description", "行程支付");
        transaction2.put("createTime", LocalDateTime.now().minusDays(2).toString());
        transaction2.put("status", "success");
        transactions.add(transaction2);
        
        return transactions;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        // 模拟检查手机号
        return userStorage.values().stream()
                .anyMatch(user -> phone.equals(user.getPhone()));
    }

    @Override
    public boolean sendSmsCode(String phone, String type) {
        // 模拟发送短信验证码
        // 在实际应用中，这里会调用短信服务提供商的API
        System.out.println("发送验证码到手机: " + phone + ", 类型: " + type);
        return true;
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        // 模拟验证短信验证码
        // 在实际应用中，这里会验证存储在缓存中的验证码
        return "123456".equals(code); // 简单模拟
    }

    @Override
    public Map<String, Object> getVerificationStatus(String userId) {
        User user = userStorage.get(userId);
        Map<String, Object> status = new HashMap<>();
        
        if (user != null) {
            status.put("verificationStatus", user.getVerificationStatus());
            status.put("submittedAt", user.getUpdateTime());
            status.put("reviewMessage", getReviewMessage(user.getVerificationStatus()));
        } else {
            status.put("verificationStatus", "none");
            status.put("submittedAt", null);
            status.put("reviewMessage", "");
        }
        
        return status;
    }

    @Override
    public boolean clearUserCache(String userId) {
        // 模拟清除用户缓存
        // 在实际应用中，这里会清除Redis或其他缓存中的用户数据
        System.out.println("清除用户缓存: " + userId);
        return true;
    }
    
    private String getReviewMessage(String status) {
        switch (status) {
            case "pending":
                return "认证材料审核中，请耐心等待";
            case "verified":
                return "认证通过，可以发布车主行程";
            case "rejected":
                return "认证未通过，请重新提交正确的认证材料";
            default:
                return "未提交认证";
        }
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