package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
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
     * 获取当前用户ID（实际项目中应该从JWT token或session中获取）
     */
    private String getCurrentUserId() {
        // 这里应该从认证信息中获取真实的用户ID
        // 为了演示，返回一个固定的用户ID
        return "user_001";
    }
}