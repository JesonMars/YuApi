package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.dto.CommuteSetupRequest;
import com.pingo.yuapi.dto.LocationDTO;
import com.pingo.yuapi.dto.UserCommuteSetup;
import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.entity.UserLocation;
import com.pingo.yuapi.mapper.UserLocationMapper;
import com.pingo.yuapi.mapper.UserMapper;
import com.pingo.yuapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLocationMapper userLocationMapper;

    @Override
    public User getUserById(String userId) {
        try {
            User user = userMapper.findById(userId);
            if (user == null) {
                user = new User();
                user.setId(userId);
                user.setName("用户" + userId.substring(Math.max(0, userId.length() - 3)));
                user.setPhone("138****8888");
                user.setAvatar("/static/default-avatar.png");
                user.setBalance(new BigDecimal("0"));
                user.setCoupons(2);
                user.setHistoryOrders(15);
                user.setVerificationStatus("none");
                user.setCreateTime(LocalDateTime.now());
                userMapper.createUser(user);
            }
            return user;
        } catch (Exception e) {
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateUser(User user) {
        try {
            int result = userMapper.updateUser(user);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新用户信息失败: " + e.getMessage());
        }
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
        try {
            String userId = (String) verificationData.get("userId");
            int result = userMapper.updateVerificationStatus(userId, "pending");
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("提交认证失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadAvatar(String userId, MultipartFile file) {
        try {
            String filename = userId + "_avatar_" + System.currentTimeMillis() + ".jpg";
            String avatarUrl = "/uploads/avatars/" + filename;
            
            userMapper.updateAvatar(userId, avatarUrl);
            
            return avatarUrl;
        } catch (Exception e) {
            throw new RuntimeException("上传头像失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadVerificationFile(String userId, MultipartFile file, String fileType) {
        // 模拟认证文件上传
        String filename = userId + "_" + fileType + "_" + System.currentTimeMillis() + ".jpg";
        return "/uploads/verification/" + filename;
    }

    @Override
    public boolean updateNickname(String userId, String nickname) {
        try {
            int result = userMapper.updateName(userId, nickname);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新昵称失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserStats(String userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            User user = userMapper.findById(userId);
            
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
        } catch (Exception e) {
            throw new RuntimeException("获取用户统计失败: " + e.getMessage());
        }
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
        try {
            User user = userMapper.findById(userId);
            if (user != null) {
                BigDecimal currentBalance = user.getBalance();
                BigDecimal newBalance = currentBalance.add(new BigDecimal(amount.toString()));
                user.setBalance(newBalance);
                userMapper.updateUser(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("钱包充值失败: " + e.getMessage());
        }
    }

    @Override
    public boolean withdrawWallet(String userId, Double amount, String bankAccount) {
        try {
            User user = userMapper.findById(userId);
            if (user != null) {
                BigDecimal currentBalance = user.getBalance();
                BigDecimal withdrawAmount = new BigDecimal(amount.toString());
                
                if (currentBalance.compareTo(withdrawAmount) >= 0) {
                    BigDecimal newBalance = currentBalance.subtract(withdrawAmount);
                    user.setBalance(newBalance);
                    userMapper.updateUser(user);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("钱包提现失败: " + e.getMessage());
        }
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
        try {
            // 在实际应用中，应该有专门的查询方法
            // 这里简化处理，返回false表示不存在
            return false;
        } catch (Exception e) {
            throw new RuntimeException("检查手机号失败: " + e.getMessage());
        }
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
        try {
            User user = userMapper.findById(userId);
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
        } catch (Exception e) {
            throw new RuntimeException("获取认证状态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateFirstSetupCompleted(String openid, boolean completed) {
        try {
            int result = userMapper.updateFirstSetupCompleted(openid, completed);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新首次设置状态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean clearUserCache(String userId) {
        // 模拟清除用户缓存
        // 在实际应用中，这里会清除Redis或其他缓存中的用户数据
        System.out.println("清除用户缓存: " + userId);
        return true;
    }

    @Override
    public UserCommuteSetup getCommuteSetup(String userId) {
        try {
            // Query home location
            UserLocation home = userLocationMapper.findByUserIdAndType(userId, "home");
            // Query company location
            UserLocation company = userLocationMapper.findByUserIdAndType(userId, "company");

            // If neither home nor company exists, return null
            if (home == null && company == null) {
                return null;
            }

            // Build response DTO
            UserCommuteSetup setup = new UserCommuteSetup();

            if (home != null) {
                setup.setHomeAddress(home.getName());
                setup.setHomeCity(home.getCity());
                setup.setHomeLocation(new LocationDTO(home.getLongitude(), home.getLatitude()));
                setup.setSetupTime(home.getCreateTime());
            }

            if (company != null) {
                setup.setWorkAddress(company.getName());
                setup.setWorkCity(company.getCity());
                setup.setWorkLocation(new LocationDTO(company.getLongitude(), company.getLatitude()));
                // Use the latest create time
                if (setup.getSetupTime() == null || company.getCreateTime().isAfter(setup.getSetupTime())) {
                    setup.setSetupTime(company.getCreateTime());
                }
            }

            return setup;
        } catch (Exception e) {
            throw new RuntimeException("获取通勤设置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean saveCommuteSetup(CommuteSetupRequest request) {
        try {
            // Save or update home location
            if (request.getHomeAddress() != null && request.getHomeLocation() != null) {
                saveOrUpdateUserLocation(
                    request.getUserId(),
                    "home",
                    request.getHomeAddress(),
                    request.getHomeCity(),
                    request.getHomeLocation()
                );
            }

            // Save or update company location
            if (request.getWorkAddress() != null && request.getWorkLocation() != null) {
                saveOrUpdateUserLocation(
                    request.getUserId(),
                    "company",
                    request.getWorkAddress(),
                    request.getWorkCity(),
                    request.getWorkLocation()
                );
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("保存通勤设置失败: " + e.getMessage());
        }
    }

    /**
     * Private helper method to save or update a user location
     */
    private void saveOrUpdateUserLocation(String userId, String type, String address, String city, LocationDTO location) {
        // Check if location already exists
        UserLocation existing = userLocationMapper.findByUserIdAndType(userId, type);

        if (existing != null) {
            // Update existing location
            existing.setName(address);
            existing.setAddress(address);
            existing.setCity(city);
            existing.setLongitude(location.getLongitude());
            existing.setLatitude(location.getLatitude());
            userLocationMapper.update(existing);
        } else {
            // Insert new location
            UserLocation newLocation = new UserLocation();
            newLocation.setId(UUID.randomUUID().toString());
            newLocation.setUserId(userId);
            newLocation.setName(address);
            newLocation.setAddress(address);
            newLocation.setCity(city);
            newLocation.setLongitude(location.getLongitude());
            newLocation.setLatitude(location.getLatitude());
            newLocation.setType(type);
            userLocationMapper.insert(newLocation);
        }
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

}