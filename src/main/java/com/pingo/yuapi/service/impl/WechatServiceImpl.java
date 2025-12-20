package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.User;
import com.pingo.yuapi.entity.WechatUser;
import com.pingo.yuapi.mapper.UserMapper;
import com.pingo.yuapi.mapper.WechatUserMapper;
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
import com.pingo.yuapi.utils.IdGeneratorUtils;

@Service
public class WechatServiceImpl implements WechatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    // 微信小程序配置（实际项目中应该从配置文件读取）
    @Value("${wechat.miniprogram.appid:wx1234567890abcdef}")
    private String appId;

    @Value("${wechat.miniprogram.secret:your-secret-key}")
    private String appSecret;

    @Override
    public Map<String, Object> wechatLogin(String code) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用微信API获取session_key和openid
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code);

            // 调用微信官方API获取session_key和openid
            Map<String, Object> wechatResponse = callWechatAPI(url, "GET", null);

            if (wechatResponse.get("errcode") != null && !Integer.valueOf(0).equals(wechatResponse.get("errcode"))) {
                throw new RuntimeException("微信登录失败: " + wechatResponse.get("errmsg"));
            }

            String openid = (String) wechatResponse.get("openid");
            String sessionKey = (String) wechatResponse.get("session_key");
            String unionid = (String) wechatResponse.get("unionid");

            // 检查或更新微信用户记录
            WechatUser existingWechatUser = wechatUserMapper.findByOpenid(openid);
            if (existingWechatUser == null) {
                // 创建新的微信用户记录
                WechatUser wechatUser = new WechatUser();
                wechatUser.setId(IdGeneratorUtils.generateId());
                wechatUser.setOpenid(openid);
                wechatUser.setUnionid(unionid);
                wechatUser.setSessionKey(sessionKey);
                wechatUserMapper.createWechatUser(wechatUser);
            } else {
                // 更新session_key
                wechatUserMapper.updateSessionKey(openid, sessionKey);
            }

            // 检查用户是否已存在
            User existingUser = userMapper.findByWechatOpenid(openid);
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
            Map<String, Object> userInfo = decryptWechatUserInfo(encryptedData, iv, sessionKey);
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("解密用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> decryptPhoneNumber(String encryptedData, String iv, String sessionKey) {
        try {
            // AES解密手机号（这里简化实现）
            Map<String, Object> phoneInfo = decryptWechatPhoneNumber(encryptedData, iv, sessionKey);
            return phoneInfo;
        } catch (Exception e) {
            throw new RuntimeException("解密手机号失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> saveUserInfo(String openid, Map<String, Object> userInfo) {
        try {
            // 更新微信用户信息
            WechatUser wechatUser = wechatUserMapper.findByOpenid(openid);
            if (wechatUser != null) {
                wechatUser.setNickName((String) userInfo.get("nickName"));
                wechatUser.setAvatarUrl((String) userInfo.get("avatarUrl"));
                wechatUser.setGender((Integer) userInfo.get("gender"));
                wechatUser.setCity((String) userInfo.get("city"));
                wechatUser.setProvince((String) userInfo.get("province"));
                wechatUser.setCountry((String) userInfo.get("country"));
                wechatUser.setLanguage((String) userInfo.get("language"));
                wechatUserMapper.updateUserInfo(wechatUser);
            }

            // 创建或更新用户信息
            User user = userMapper.findByWechatOpenid(openid);
            if (user == null) {
                // 创建新用户
                user = new User();
                user.setId(IdGeneratorUtils.generateId());
                user.setName((String) userInfo.get("nickName"));
                user.setAvatar((String) userInfo.get("avatarUrl"));
                user.setWechatOpenid(openid);
                user.setFirstSetupCompleted(false);
                user.setBalance(new BigDecimal("0.00"));
                user.setCoupons(0);
                user.setHistoryOrders(0);
                user.setVerificationStatus("none");
                userMapper.createUser(user);
            } else {
                // 更新现有用户
                user.setName((String) userInfo.get("nickName"));
                user.setAvatar((String) userInfo.get("avatarUrl"));
                userMapper.updateUser(user);
            }

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
            WechatUser wechatUser = wechatUserMapper.findByOpenid(openid);
            if (wechatUser != null) {
                // 更新数据库中的用户信息
                User user = userMapper.findByWechatOpenid(openid);
                if (user != null) {
                    user.setPhone(phoneNumber);
                    userMapper.updateUser(user);
                }

                // 更新微信用户的手机号
                wechatUserMapper.updatePhoneNumber(openid, phoneNumber);
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
        User user = userMapper.findByWechatOpenid(openid);
        if (user != null) {
            WechatUser wechatUser = wechatUserMapper.findByOpenid(openid);
            Map<String, Object> result = convertUserToMap(user);
            if (wechatUser != null) {
                Map<String, Object> wechatInfo = new HashMap<>();
                wechatInfo.put("nickName", wechatUser.getNickName());
                wechatInfo.put("avatarUrl", wechatUser.getAvatarUrl());
                wechatInfo.put("gender", wechatUser.getGender());
                wechatInfo.put("city", wechatUser.getCity());
                wechatInfo.put("province", wechatUser.getProvince());
                wechatInfo.put("country", wechatUser.getCountry());
                wechatInfo.put("phoneNumber", wechatUser.getPhoneNumber());
                result.put("wechatInfo", wechatInfo);
            }
            return result;
        }
        return null;
    }

    @Override
    public Map<String, Object> getAuthStatus(String openid) {
        Map<String, Object> status = new HashMap<>();
        WechatUser wechatUser = wechatUserMapper.findByOpenid(openid);
        User user = userMapper.findByWechatOpenid(openid);

        if (wechatUser != null) {
            status.put("hasUserInfo", wechatUser.getNickName() != null);
            status.put("hasPhone", wechatUser.getPhoneNumber() != null);
            status.put("nickName", wechatUser.getNickName());
            status.put("avatarUrl", wechatUser.getAvatarUrl());
        } else {
            status.put("hasUserInfo", false);
            status.put("hasPhone", false);
        }

        if (user != null) {
            status.put("firstSetupCompleted", user.getFirstSetupCompleted());
        } else {
            status.put("firstSetupCompleted", false);
        }

        status.put("openid", openid);
        return status;
    }

    @Override
    public String getAccessToken() {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    appId, appSecret);

            Map<String, Object> response = callWechatAPI(url, "GET", null);
            if (response.get("errcode") != null && !Integer.valueOf(0).equals(response.get("errcode"))) {
                throw new RuntimeException("获取AccessToken失败: " + response.get("errmsg"));
            }
            return (String) response.get("access_token");
        } catch (Exception e) {
            throw new RuntimeException("获取AccessToken失败: " + e.getMessage());
        }
    }

    @Override
    public String generateQRCode(String scene, String page) {
        try {
            String accessToken = getAccessToken();
            String url = String.format(
                    "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s",
                    accessToken);

            Map<String, Object> data = new HashMap<>();
            data.put("scene", scene);
            if (page != null) {
                data.put("page", page);
            }

            // 调用微信API生成二维码，返回二进制数据
            byte[] qrCodeBytes = callWechatBinaryAPI(url, data);

            // 保存二维码到存储并返回可访问的URL
            return saveQRCodeToStorage(qrCodeBytes, scene);
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data, String page) {
        try {
            String accessToken = getAccessToken();
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s",
                    accessToken);

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("touser", openid);
            requestData.put("template_id", templateId);
            requestData.put("data", data);
            if (page != null) {
                requestData.put("page", page);
            }

            Map<String, Object> response = callWechatAPI(url, "POST", requestData);
            return Integer.valueOf(0).equals(response.get("errcode"));
        } catch (Exception e) {
            throw new RuntimeException("发送模板消息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> refreshUserSession(String openid) {
        // 从数据库获取用户会话信息
        WechatUser wechatUser = wechatUserMapper.findByOpenid(openid);
        if (wechatUser != null) {
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("openid", openid);
            sessionInfo.put("sessionKey", wechatUser.getSessionKey());
            sessionInfo.put("refreshTime", LocalDateTime.now());
            return sessionInfo;
        }
        return null;
    }

    // 调用微信API的通用方法
    private Map<String, Object> callWechatAPI(String url, String method, Map<String, Object> data) {
        try {
            System.out.println("调用微信API: " + url);
            String response;

            if ("GET".equals(method)) {
                response = restTemplate.getForObject(url, String.class);
            } else {
                response = restTemplate.postForObject(url, data, String.class);
            }

            System.out.println("微信API响应: " + response);

            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("微信API返回空响应");
            }

            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            System.err.println("调用微信API失败 - URL: " + url + ", 错误: " + e.getMessage());
            throw new RuntimeException("调用微信API失败: " + e.getMessage());
        }
    }

    // 调用微信API返回二进制数据（如二维码）
    private byte[] callWechatBinaryAPI(String url, Map<String, Object> data) {
        try {
            return restTemplate.postForObject(url, data, byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("调用微信二进制API失败: " + e.getMessage());
        }
    }

    // 真实的微信用户信息解密
    private Map<String, Object> decryptWechatUserInfo(String encryptedData, String iv, String sessionKey) {
        try {
            // 使用AES-128-CBC解密
            byte[] sessionKeyBytes = Base64.decodeBase64(sessionKey);
            byte[] ivBytes = Base64.decodeBase64(iv);
            byte[] encryptedBytes = Base64.decodeBase64(encryptedData);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(decryptedData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解密用户信息失败: " + e.getMessage());
        }
    }

    // 真实的微信手机号解密
    private Map<String, Object> decryptWechatPhoneNumber(String encryptedData, String iv, String sessionKey) {
        try {
            // 使用AES-128-CBC解密
            byte[] sessionKeyBytes = Base64.decodeBase64(sessionKey);
            byte[] ivBytes = Base64.decodeBase64(iv);
            byte[] encryptedBytes = Base64.decodeBase64(encryptedData);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(decryptedData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解密手机号失败: " + e.getMessage());
        }
    }

    // 保存二维码到存储
    private String saveQRCodeToStorage(byte[] qrCodeBytes, String scene) {
        try {
            // 这里应该保存到文件系统或对象存储，返回可访问的URL
            // 示例：保存到本地文件系统
            String fileName = "qrcode_" + scene + "_" + System.currentTimeMillis() + ".png";
            String filePath = "/uploads/qrcodes/" + fileName;

            // 实际项目中需要创建目录和文件
            // Files.write(Paths.get(filePath), qrCodeBytes);

            return "https://yourdomain.com" + filePath;
        } catch (Exception e) {
            throw new RuntimeException("保存二维码失败: " + e.getMessage());
        }
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