package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    
    /**
     * 根据ID获取用户
     */
    User getUserById(String userId);
    
    /**
     * 更新用户
     */
    boolean updateUser(User user);
    
    /**
     * 获取用户位置列表
     */
    List<Map<String, Object>> getUserLocations(String userId);
    
    /**
     * 保存用户位置
     */
    String saveUserLocation(Map<String, Object> location);
    
    /**
     * 获取关注的司机列表
     */
    List<Map<String, Object>> getFollowedDrivers(String userId);
    
    /**
     * 关注司机
     */
    boolean followDriver(String userId, String driverId);
    
    /**
     * 取消关注司机
     */
    boolean unfollowDriver(String userId, String driverId);
    
    /**
     * 获取黑名单用户列表
     */
    List<Map<String, Object>> getBlacklistedUsers(String userId);
    
    /**
     * 添加到黑名单
     */
    boolean addToBlacklist(String userId, String targetUserId, String reason);
    
    /**
     * 从黑名单移除
     */
    boolean removeFromBlacklist(String userId, String targetUserId);
    
    /**
     * 提交车主认证
     */
    boolean submitDriverVerification(Map<String, Object> verificationData);
    
    /**
     * 上传头像
     */
    String uploadAvatar(String userId, MultipartFile file);
    
    /**
     * 上传认证文件
     */
    String uploadVerificationFile(String userId, MultipartFile file, String fileType);
    
    /**
     * 更新用户昵称
     */
    boolean updateNickname(String userId, String nickname);
    
    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats(String userId);
    
    /**
     * 获取用户设置
     */
    Map<String, Object> getUserSettings(String userId);
    
    /**
     * 更新用户设置
     */
    boolean updateUserSettings(String userId, Map<String, Object> settings);
    
    /**
     * 获取用户钱包信息
     */
    Map<String, Object> getUserWallet(String userId);
    
    /**
     * 钱包充值
     */
    boolean rechargeWallet(String userId, Double amount, String paymentMethod);
    
    /**
     * 钱包提现
     */
    boolean withdrawWallet(String userId, Double amount, String bankAccount);
    
    /**
     * 获取钱包交易记录
     */
    List<Map<String, Object>> getWalletTransactions(String userId, Integer page, Integer limit);
    
    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone);
    
    /**
     * 发送短信验证码
     */
    boolean sendSmsCode(String phone, String type);
    
    /**
     * 验证短信验证码
     */
    boolean verifySmsCode(String phone, String code);
    
    /**
     * 获取用户认证状态
     */
    Map<String, Object> getVerificationStatus(String userId);
    
    /**
     * 清除用户缓存
     */
    boolean clearUserCache(String userId);
}