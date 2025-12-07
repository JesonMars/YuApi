package com.pingo.yuapi.service;

import java.util.Map;

public interface AuthService {
    
    /**
     * 微信登录
     */
    Map<String, Object> wechatLogin(Map<String, Object> loginData);
    
    /**
     * 绑定手机号
     */
    boolean bindPhoneNumber(String token, Map<String, String> phoneData);
    
    /**
     * 检查token是否有效
     */
    boolean checkTokenValid(String token);
    
    /**
     * 刷新token
     */
    String refreshToken(String refreshToken);
    
    /**
     * 退出登录
     */
    boolean logout(String token);
    
    /**
     * 根据token获取用户信息
     */
    Map<String, Object> getUserInfoByToken(String token);
}