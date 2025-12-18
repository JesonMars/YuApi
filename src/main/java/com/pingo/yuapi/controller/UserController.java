package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.dto.CommuteSetupRequest;
import com.pingo.yuapi.dto.UserCommuteSetup;
import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    public Result<User> getUserProfile() {
        try {
            String userId = getCurrentUserId();
            User user = userService.getUserById(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public Result<Boolean> updateUserProfile(@RequestBody User user) {
        try {
            String userId = getCurrentUserId();
            user.setId(userId);
            boolean success = userService.updateUser(user);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新用户资料失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户位置列表
     */
    @GetMapping("/locations")
    public Result<List<Map<String, Object>>> getUserLocations() {
        try {
            String userId = getCurrentUserId();
            List<Map<String, Object>> locations = userService.getUserLocations(userId);
            return Result.success(locations);
        } catch (Exception e) {
            return Result.error("获取用户位置失败: " + e.getMessage());
        }
    }

    /**
     * 保存用户位置
     */
    @PostMapping("/locations")
    public Result<String> saveUserLocation(@RequestBody Map<String, Object> location) {
        try {
            String userId = getCurrentUserId();
            location.put("userId", userId);
            String locationId = userService.saveUserLocation(location);
            return Result.success(locationId);
        } catch (Exception e) {
            return Result.error("保存位置失败: " + e.getMessage());
        }
    }

    /**
     * 获取关注的司机列表
     */
    @GetMapping("/follows")
    public Result<List<Map<String, Object>>> getFollowedDrivers() {
        try {
            String userId = getCurrentUserId();
            List<Map<String, Object>> drivers = userService.getFollowedDrivers(userId);
            return Result.success(drivers);
        } catch (Exception e) {
            return Result.error("获取关注列表失败: " + e.getMessage());
        }
    }

    /**
     * 关注司机
     */
    @PostMapping("/follow")
    public Result<Boolean> followDriver(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String driverId = request.get("driverId");
            boolean success = userService.followDriver(userId, driverId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("关注司机失败: " + e.getMessage());
        }
    }

    /**
     * 取消关注司机
     */
    @DeleteMapping("/unfollow")
    public Result<Boolean> unfollowDriver(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String driverId = request.get("driverId");
            boolean success = userService.unfollowDriver(userId, driverId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("取消关注失败: " + e.getMessage());
        }
    }

    /**
     * 获取黑名单用户列表
     */
    @GetMapping("/blacklist")
    public Result<List<Map<String, Object>>> getBlacklistedUsers() {
        try {
            String userId = getCurrentUserId();
            List<Map<String, Object>> blacklistedUsers = userService.getBlacklistedUsers(userId);
            return Result.success(blacklistedUsers);
        } catch (Exception e) {
            return Result.error("获取黑名单失败: " + e.getMessage());
        }
    }

    /**
     * 添加用户到黑名单
     */
    @PostMapping("/blacklist")
    public Result<Boolean> addToBlacklist(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String targetUserId = request.get("userId");
            String reason = request.get("reason");
            boolean success = userService.addToBlacklist(userId, targetUserId, reason);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("添加黑名单失败: " + e.getMessage());
        }
    }

    /**
     * 从黑名单移除用户
     */
    @DeleteMapping("/blacklist")
    public Result<Boolean> removeFromBlacklist(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String targetUserId = request.get("userId");
            boolean success = userService.removeFromBlacklist(userId, targetUserId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("移除黑名单失败: " + e.getMessage());
        }
    }

    /**
     * 提交车主认证
     */
    @PostMapping("/verification")
    public Result<Boolean> submitDriverVerification(@RequestBody Map<String, Object> verificationData) {
        try {
            String userId = getCurrentUserId();
            verificationData.put("userId", userId);
            boolean success = userService.submitDriverVerification(verificationData);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("提交认证失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        try {
            String userId = getCurrentUserId();
            String avatarUrl = userService.uploadAvatar(userId, file);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 上传认证文件
     */
    @PostMapping("/verification/upload")
    public Result<String> uploadVerificationFile(@RequestParam("file") MultipartFile file, 
                                                 @RequestParam("type") String fileType) {
        try {
            String userId = getCurrentUserId();
            String fileUrl = userService.uploadVerificationFile(userId, file, fileType);
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户昵称
     */
    @PutMapping("/nickname")
    public Result<Boolean> updateNickname(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String nickname = request.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return Result.error("昵称不能为空");
            }
            boolean success = userService.updateNickname(userId, nickname.trim());
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新昵称失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> stats = userService.getUserStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户设置
     */
    @GetMapping("/settings")
    public Result<Map<String, Object>> getUserSettings() {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> settings = userService.getUserSettings(userId);
            return Result.success(settings);
        } catch (Exception e) {
            return Result.error("获取用户设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户设置
     */
    @PutMapping("/settings")
    public Result<Boolean> updateUserSettings(@RequestBody Map<String, Object> settings) {
        try {
            String userId = getCurrentUserId();
            boolean success = userService.updateUserSettings(userId, settings);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新用户设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户钱包信息
     */
    @GetMapping("/wallet")
    public Result<Map<String, Object>> getUserWallet() {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> walletInfo = userService.getUserWallet(userId);
            return Result.success(walletInfo);
        } catch (Exception e) {
            return Result.error("获取钱包信息失败: " + e.getMessage());
        }
    }

    /**
     * 钱包充值
     */
    @PostMapping("/wallet/recharge")
    public Result<Boolean> rechargeWallet(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Double amount = Double.valueOf(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            
            if (amount <= 0) {
                return Result.error("充值金额必须大于0");
            }
            
            boolean success = userService.rechargeWallet(userId, amount, paymentMethod);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("钱包充值失败: " + e.getMessage());
        }
    }

    /**
     * 钱包提现
     */
    @PostMapping("/wallet/withdraw")
    public Result<Boolean> withdrawWallet(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Double amount = Double.valueOf(request.get("amount").toString());
            String bankAccount = (String) request.get("bankAccount");
            
            if (amount <= 0) {
                return Result.error("提现金额必须大于0");
            }
            
            boolean success = userService.withdrawWallet(userId, amount, bankAccount);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("钱包提现失败: " + e.getMessage());
        }
    }

    /**
     * 获取钱包交易记录
     */
    @GetMapping("/wallet/transactions")
    public Result<List<Map<String, Object>>> getWalletTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            String userId = getCurrentUserId();
            List<Map<String, Object>> transactions = userService.getWalletTransactions(userId, page, limit);
            return Result.success(transactions);
        } catch (Exception e) {
            return Result.error("获取交易记录失败: " + e.getMessage());
        }
    }

    /**
     * 检查手机号是否已存在
     */
    @GetMapping("/check-phone/{phone}")
    public Result<Boolean> checkPhoneExists(@PathVariable String phone) {
        try {
            boolean exists = userService.checkPhoneExists(phone);
            return Result.success(exists);
        } catch (Exception e) {
            return Result.error("检查手机号失败: " + e.getMessage());
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-sms")
    public Result<Boolean> sendSmsCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String type = request.get("type"); // register, login, reset_password
            boolean success = userService.sendSmsCode(phone, type);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("发送验证码失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-sms")
    public Result<Boolean> verifySmsCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String code = request.get("code");
            boolean success = userService.verifySmsCode(phone, code);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户认证状态
     */
    @GetMapping("/verification-status")
    public Result<Map<String, Object>> getVerificationStatus() {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> status = userService.getVerificationStatus(userId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("获取认证状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新首次设置完成状态
     */
    @PutMapping("/setup/complete")
    public Result<Boolean> updateFirstSetupCompleted(@RequestBody Map<String, Object> request) {
        try {
            String openid = (String) request.get("openid");
            Boolean completed = (Boolean) request.get("completed");
            
            if (openid == null || completed == null) {
                return Result.error("参数不完整");
            }
            
            boolean success = userService.updateFirstSetupCompleted(openid, completed);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新设置状态失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户缓存
     */
    @DeleteMapping("/cache")
    public Result<Boolean> clearUserCache() {
        try {
            String userId = getCurrentUserId();
            boolean success = userService.clearUserCache(userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户通勤设置
     */
    @GetMapping("/commute-setup")
    public Result<UserCommuteSetup> getCommuteSetup(@RequestParam String userId) {
        try {
            UserCommuteSetup setup = userService.getCommuteSetup(userId);
            return Result.success(setup);
        } catch (Exception e) {
            return Result.error("获取通勤设置失败: " + e.getMessage());
        }
    }

    /**
     * 保存用户通勤设置
     */
    @PostMapping("/commute-setup")
    public Result<Boolean> saveCommuteSetup(@RequestBody CommuteSetupRequest request) {
        try {
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }

            boolean success = userService.saveCommuteSetup(request);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("保存通勤设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID（实际项目中应该从JWT token或session中获取）
     */
    private String getCurrentUserId() {
        // 这里应该从认证信息中获取真实的用户ID
        // 为了演示，返回一个固定的用户ID
        return "user_001";
    }
}