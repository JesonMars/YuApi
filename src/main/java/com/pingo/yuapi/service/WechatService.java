package com.pingo.yuapi.service;

import java.util.Map;

public interface WechatService {
    
    /**
     * 微信小程序登录
     * @param code 微信登录code
     * @return 包含openid、session_key等信息
     */
    Map<String, Object> wechatLogin(String code);
    
    /**
     * 解密用户信息
     * @param encryptedData 加密数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     * @return 解密后的用户信息
     */
    Map<String, Object> decryptUserInfo(String encryptedData, String iv, String sessionKey);
    
    /**
     * 解密手机号信息
     * @param encryptedData 加密数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     * @return 解密后的手机号信息
     */
    Map<String, Object> decryptPhoneNumber(String encryptedData, String iv, String sessionKey);
    
    /**
     * 保存用户信息
     * @param openid 用户openid
     * @param userInfo 用户信息
     * @return 保存结果
     */
    Map<String, Object> saveUserInfo(String openid, Map<String, Object> userInfo);
    
    /**
     * 更新用户手机号
     * @param openid 用户openid
     * @param phoneInfo 手机号信息
     * @return 更新结果
     */
    Map<String, Object> updateUserPhone(String openid, Map<String, Object> phoneInfo);
    
    /**
     * 根据openid获取用户信息
     * @param openid 用户openid
     * @return 用户信息
     */
    Map<String, Object> getUserByOpenid(String openid);
    
    /**
     * 获取用户授权状态
     * @param openid 用户openid
     * @return 授权状态信息
     */
    Map<String, Object> getAuthStatus(String openid);
    
    /**
     * 获取微信Access Token
     * @return Access Token
     */
    String getAccessToken();
    
    /**
     * 生成小程序码
     * @param scene 场景值
     * @param page 页面路径
     * @return 小程序码URL
     */
    String generateQRCode(String scene, String page);
    
    /**
     * 发送模板消息
     * @param openid 用户openid
     * @param templateId 模板ID
     * @param data 模板数据
     * @param page 跳转页面
     * @return 发送结果
     */
    boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data, String page);
    
    /**
     * 刷新用户session
     * @param openid 用户openid
     * @return 新的session信息
     */
    Map<String, Object> refreshUserSession(String openid);
}