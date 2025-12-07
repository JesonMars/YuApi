package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.service.AuthService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    // 模拟token存储
    private Map<String, Map<String, Object>> tokenStorage = new HashMap<>();
    
    // 模拟用户存储
    private Map<String, Map<String, Object>> userStorage = new HashMap<>();
    
    // 模拟refresh token存储
    private Map<String, String> refreshTokenStorage = new HashMap<>();

    @Override
    public Map<String, Object> wechatLogin(Map<String, Object> loginData) {
        String code = (String) loginData.get("code");
        Map<String, Object> userInfo = (Map<String, Object>) loginData.get("userInfo");
        
        // 模拟微信API调用，获取openid和session_key
        String openid = "mock_openid_" + code.substring(0, Math.min(8, code.length()));
        String sessionKey = "mock_session_key_" + UUID.randomUUID().toString().substring(0, 8);
        
        // 检查用户是否已存在
        String userId = findUserByOpenid(openid);
        boolean isFirstLogin = userId == null;
        
        if (isFirstLogin) {
            // 创建新用户
            userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", userId);
            userData.put("openid", openid);
            userData.put("sessionKey", sessionKey);
            
            if (userInfo != null) {
                userData.put("nickName", userInfo.get("nickName"));
                userData.put("avatarUrl", userInfo.get("avatarUrl"));
                userData.put("gender", userInfo.get("gender"));
                userData.put("country", userInfo.get("country"));
                userData.put("province", userInfo.get("province"));
                userData.put("city", userInfo.get("city"));
            }
            
            userData.put("phone", null);
            userData.put("isComplete", false);
            userData.put("createTime", new Date());
            
            userStorage.put(userId, userData);
        } else {
            // 更新现有用户信息
            Map<String, Object> userData = userStorage.get(userId);
            if (userInfo != null) {
                userData.put("nickName", userInfo.get("nickName"));
                userData.put("avatarUrl", userInfo.get("avatarUrl"));
            }
            userData.put("sessionKey", sessionKey);
        }
        
        // 生成token
        String token = generateToken(userId);
        String refreshToken = generateRefreshToken(userId);
        
        // 存储token信息
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userId", userId);
        tokenData.put("openid", openid);
        tokenData.put("createTime", new Date());
        tokenData.put("expiresAt", new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)); // 7天过期
        
        tokenStorage.put(token, tokenData);
        refreshTokenStorage.put(refreshToken, userId);
        
        // 准备返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", userId);
        result.put("isFirstLogin", isFirstLogin);
        
        Map<String, Object> userResult = new HashMap<>();
        Map<String, Object> userData = userStorage.get(userId);
        userResult.put("id", userId);
        userResult.put("nickName", userData.get("nickName"));
        userResult.put("avatarUrl", userData.get("avatarUrl"));
        userResult.put("phone", userData.get("phone"));
        userResult.put("isComplete", userData.get("phone") != null);
        
        result.put("userInfo", userResult);
        
        return result;
    }

    @Override
    public boolean bindPhoneNumber(String token, Map<String, String> phoneData) {
        Map<String, Object> tokenData = tokenStorage.get(token);
        if (tokenData == null) {
            throw new RuntimeException("Token无效");
        }
        
        String userId = (String) tokenData.get("userId");
        String encryptedData = phoneData.get("encryptedData");
        String iv = phoneData.get("iv");
        
        // 模拟解密手机号（实际项目中需要使用微信提供的解密方法）
        String phoneNumber = mockDecryptPhoneNumber(encryptedData, iv);
        
        // 更新用户手机号
        Map<String, Object> userData = userStorage.get(userId);
        userData.put("phone", phoneNumber);
        userData.put("isComplete", true);
        userData.put("updateTime", new Date());
        
        return true;
    }

    @Override
    public boolean checkTokenValid(String token) {
        Map<String, Object> tokenData = tokenStorage.get(token);
        if (tokenData == null) {
            return false;
        }
        
        Date expiresAt = (Date) tokenData.get("expiresAt");
        return expiresAt.after(new Date());
    }

    @Override
    public String refreshToken(String refreshToken) {
        String userId = refreshTokenStorage.get(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Refresh token无效");
        }
        
        // 生成新的token
        String newToken = generateToken(userId);
        
        // 存储新token信息
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userId", userId);
        tokenData.put("createTime", new Date());
        tokenData.put("expiresAt", new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
        
        tokenStorage.put(newToken, tokenData);
        
        return newToken;
    }

    @Override
    public boolean logout(String token) {
        Map<String, Object> tokenData = tokenStorage.remove(token);
        return tokenData != null;
    }

    @Override
    public Map<String, Object> getUserInfoByToken(String token) {
        Map<String, Object> tokenData = tokenStorage.get(token);
        if (tokenData == null) {
            throw new RuntimeException("Token无效");
        }
        
        String userId = (String) tokenData.get("userId");
        Map<String, Object> userData = userStorage.get(userId);
        
        if (userData == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", userId);
        result.put("nickName", userData.get("nickName"));
        result.put("avatarUrl", userData.get("avatarUrl"));
        result.put("phone", userData.get("phone"));
        result.put("gender", userData.get("gender"));
        result.put("country", userData.get("country"));
        result.put("province", userData.get("province"));
        result.put("city", userData.get("city"));
        result.put("isComplete", userData.get("phone") != null);
        
        return result;
    }

    /**
     * 根据openid查找用户
     */
    private String findUserByOpenid(String openid) {
        for (Map.Entry<String, Map<String, Object>> entry : userStorage.entrySet()) {
            if (openid.equals(entry.getValue().get("openid"))) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 生成token
     */
    private String generateToken(String userId) {
        return "token_" + userId + "_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成refresh token
     */
    private String generateRefreshToken(String userId) {
        return "refresh_" + userId + "_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 模拟解密手机号
     */
    private String mockDecryptPhoneNumber(String encryptedData, String iv) {
        // 实际项目中需要使用微信提供的AES解密方法
        // 这里返回一个模拟的手机号
        return "138" + String.format("%08d", new Random().nextInt(100000000));
    }
}