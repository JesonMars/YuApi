package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.service.UserService;
import com.pingo.yuapi.service.WechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

@Service
public class WechatServiceImpl implements WechatService {

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    // 微信小程序配置（实际项目中应该从配置文件读取）
    @Value("${wechat.miniprogram.appid:wx1234567890abcdef}")
    private String appId;

    @Value("${wechat.miniprogram.secret:your-secret-key}")
    private String appSecret;

    // 模拟存储session信息
    private Map<String, Map<String, Object>> sessionStorage = new HashMap<>();
    
    // 模拟存储用户微信信息
    private Map<String, Map<String, Object>> wechatUserStorage = new HashMap<>();

    @Override
    public Map<String, Object> wechatLogin(String code) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用微信API获取session_key和openid
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
            );
            
            // 这里模拟微信API返回结果（实际项目中需要真实调用）
            Map<String, Object> wechatResponse = simulateWechatLoginResponse(code);
            
            if (wechatResponse.get("errcode") != null) {
                throw new RuntimeException("微信登录失败: " + wechatResponse.get("errmsg"));
            }
            
            String openid = (String) wechatResponse.get("openid");
            String sessionKey = (String) wechatResponse.get("session_key");
            String unionid = (String) wechatResponse.get("unionid");
            
            // 保存session信息
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("openid", openid);
            sessionInfo.put("sessionKey", sessionKey);
            sessionInfo.put("unionid", unionid);
            sessionInfo.put("createTime", LocalDateTime.now());
            sessionStorage.put(openid, sessionInfo);
            
            // 检查用户是否已存在
            Map<String, Object> existingUser = getUserByOpenid(openid);
            boolean isNewUser = (existingUser == null);
            
            result.put("openid", openid);
            result.put("sessionKey", sessionKey);
            result.put("unionid", unionid);
            result.put("isNewUser", isNewUser);
            
            if (!isNewUser) {
                result.put("userInfo", existingUser);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> decryptUserInfo(String encryptedData, String iv, String sessionKey) {
        try {
            // AES解密（这里简化实现，实际项目中需要完整的AES解密）
            Map<String, Object> userInfo = simulateDecryptUserInfo(encryptedData);
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("解密用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> decryptPhoneNumber(String encryptedData, String iv, String sessionKey) {
        try {
            // AES解密手机号（这里简化实现）
            Map<String, Object> phoneInfo = simulateDecryptPhoneNumber(encryptedData);
            return phoneInfo;
        } catch (Exception e) {
            throw new RuntimeException("解密手机号失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> saveUserInfo(String openid, Map<String, Object> userInfo) {
        try {
            // 创建或更新用户信息
            User user = createOrUpdateUser(openid, userInfo, null);
            
            // 保存微信用户信息
            Map<String, Object> wechatUserInfo = new HashMap<>();
            wechatUserInfo.put("openid", openid);
            wechatUserInfo.put("nickName", userInfo.get("nickName"));
            wechatUserInfo.put("avatarUrl", userInfo.get("avatarUrl"));
            wechatUserInfo.put("gender", userInfo.get("gender"));
            wechatUserInfo.put("city", userInfo.get("city"));
            wechatUserInfo.put("province", userInfo.get("province"));
            wechatUserInfo.put("country", userInfo.get("country"));
            wechatUserInfo.put("language", userInfo.get("language"));
            wechatUserInfo.put("updateTime", LocalDateTime.now());
            wechatUserStorage.put(openid, wechatUserInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("userInfo", convertUserToMap(user));
            result.put("success", true);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("保存用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> updateUserPhone(String openid, Map<String, Object> phoneInfo) {
        try {
            String phoneNumber = (String) phoneInfo.get("phoneNumber");
            String purePhoneNumber = (String) phoneInfo.get("purePhoneNumber");
            String countryCode = (String) phoneInfo.get("countryCode");
            
            // 更新用户手机号
            Map<String, Object> existingUserInfo = wechatUserStorage.get(openid);
            if (existingUserInfo != null) {
                // 更新数据库中的用户信息
                User user = userService.getUserById(getInternalUserIdByOpenid(openid));
                if (user != null) {
                    user.setPhone(phoneNumber);
                    userService.updateUser(user);
                }
                
                // 更新微信用户信息
                existingUserInfo.put("phoneNumber", phoneNumber);
                existingUserInfo.put("purePhoneNumber", purePhoneNumber);
                existingUserInfo.put("countryCode", countryCode);
                existingUserInfo.put("phoneUpdateTime", LocalDateTime.now());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("phoneNumber", phoneNumber);
            result.put("success", true);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("更新手机号失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserByOpenid(String openid) {
        Map<String, Object> wechatUserInfo = wechatUserStorage.get(openid);
        if (wechatUserInfo != null) {
            String internalUserId = getInternalUserIdByOpenid(openid);
            User user = userService.getUserById(internalUserId);
            
            if (user != null) {
                Map<String, Object> result = convertUserToMap(user);
                result.put("wechatInfo", wechatUserInfo);
                return result;
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getAuthStatus(String openid) {
        Map<String, Object> status = new HashMap<>();
        Map<String, Object> wechatUserInfo = wechatUserStorage.get(openid);
        
        if (wechatUserInfo != null) {
            status.put("hasUserInfo", true);
            status.put("hasPhone", wechatUserInfo.get("phoneNumber") != null);
            status.put("nickName", wechatUserInfo.get("nickName"));
            status.put("avatarUrl", wechatUserInfo.get("avatarUrl"));
        } else {
            status.put("hasUserInfo", false);
            status.put("hasPhone", false);
        }
        
        status.put("openid", openid);
        return status;
    }

    @Override
    public String getAccessToken() {
        // 实际项目中应该调用微信API获取access_token并缓存
        // 这里返回模拟的token
        return "mock_access_token_" + System.currentTimeMillis();
    }

    @Override
    public String generateQRCode(String scene, String page) {
        // 实际项目中调用微信API生成小程序码
        // 这里返回模拟的二维码URL
        return "https://mock-qrcode-url.com/" + scene + "?page=" + page;
    }

    @Override
    public boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data, String page) {
        try {
            // 实际项目中调用微信模板消息API
            // 这里模拟发送成功
            System.out.println("发送模板消息到: " + openid + ", 模板ID: " + templateId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> refreshUserSession(String openid) {
        Map<String, Object> sessionInfo = sessionStorage.get(openid);
        if (sessionInfo != null) {
            sessionInfo.put("refreshTime", LocalDateTime.now());
            return sessionInfo;
        }
        return null;
    }

    // 辅助方法：模拟微信登录API响应
    private Map<String, Object> simulateWechatLoginResponse(String code) {
        Map<String, Object> response = new HashMap<>();
        
        if ("mock_error_code".equals(code)) {
            response.put("errcode", 40013);
            response.put("errmsg", "invalid appid");
        } else {
            String openid = "wx_openid_" + code.substring(Math.max(0, code.length() - 8));
            String sessionKey = "session_key_" + UUID.randomUUID().toString().substring(0, 16);
            String unionid = "wx_unionid_" + code.substring(Math.max(0, code.length() - 6));
            
            response.put("openid", openid);
            response.put("session_key", sessionKey);
            response.put("unionid", unionid);
        }
        
        return response;
    }

    // 辅助方法：模拟解密用户信息
    private Map<String, Object> simulateDecryptUserInfo(String encryptedData) {
        Map<String, Object> userInfo = new HashMap<>();
        
        // 模拟解密后的用户信息
        userInfo.put("openId", "wx_openid_" + System.currentTimeMillis());
        userInfo.put("nickName", "微信用户" + (int)(Math.random() * 1000));
        userInfo.put("gender", (int)(Math.random() * 2) + 1); // 1男2女
        userInfo.put("city", "北京");
        userInfo.put("province", "北京");
        userInfo.put("country", "中国");
        userInfo.put("avatarUrl", "https://thirdwx.qlogo.cn/mmopen/mock_avatar_" + System.currentTimeMillis() + ".png");
        userInfo.put("language", "zh_CN");
        
        return userInfo;
    }

    // 辅助方法：模拟解密手机号
    private Map<String, Object> simulateDecryptPhoneNumber(String encryptedData) {
        Map<String, Object> phoneInfo = new HashMap<>();
        
        // 生成模拟手机号
        String phoneNumber = "138" + String.format("%08d", (int)(Math.random() * 100000000));
        
        phoneInfo.put("phoneNumber", phoneNumber);
        phoneInfo.put("purePhoneNumber", phoneNumber);
        phoneInfo.put("countryCode", "86");
        
        return phoneInfo;
    }

    // 辅助方法：创建或更新用户
    private User createOrUpdateUser(String openid, Map<String, Object> userInfo, String phone) {
        String internalUserId = getInternalUserIdByOpenid(openid);
        User user = userService.getUserById(internalUserId);
        
        if (user == null) {
            // 创建新用户
            user = new User();
            user.setId(internalUserId);
            user.setName((String) userInfo.get("nickName"));
            user.setAvatar((String) userInfo.get("avatarUrl"));
            if (phone != null) {
                user.setPhone(phone);
            }
            user.setBalance(new BigDecimal("0.00"));
            user.setCoupons(0);
            user.setHistoryOrders(0);
            user.setVerificationStatus("none");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
        } else {
            // 更新现有用户
            user.setName((String) userInfo.get("nickName"));
            user.setAvatar((String) userInfo.get("avatarUrl"));
            if (phone != null) {
                user.setPhone(phone);
            }
            user.setUpdateTime(LocalDateTime.now());
            userService.updateUser(user);
        }
        
        return user;
    }

    // 辅助方法：根据openid获取内部用户ID
    private String getInternalUserIdByOpenid(String openid) {
        return "user_" + openid.substring(Math.max(0, openid.length() - 8));
    }

    // 辅助方法：将User实体转换为Map
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        result.put("community", user.getCommunity());
        result.put("balance", user.getBalance());
        result.put("coupons", user.getCoupons());
        result.put("historyOrders", user.getHistoryOrders());
        result.put("verificationStatus", user.getVerificationStatus());
        result.put("createTime", user.getCreateTime());
        result.put("updateTime", user.getUpdateTime());
        return result;
    }
}