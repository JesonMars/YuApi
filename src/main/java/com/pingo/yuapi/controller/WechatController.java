package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.service.WechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wechat")
@CrossOrigin(origins = "*")
public class WechatController {

    @Autowired
    private WechatService wechatService;

    /**
     * 微信小程序登录
     * 通过code获取session_key和openid
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> wechatLogin(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return Result.error("code不能为空");
            }

            Map<String, Object> loginResult = wechatService.wechatLogin(code);
            return Result.success(loginResult);
        } catch (Exception e) {
            return Result.error("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 微信用户信息授权
     * 获取用户的微信信息（头像、昵称等）
     */
    @PostMapping("/auth/userinfo")
    public Result<Map<String, Object>> authorizeUserInfo(@RequestBody Map<String, Object> request) {
        try {
            String openid = (String) request.get("openid");
            String encryptedData = (String) request.get("encryptedData");
            String iv = (String) request.get("iv");
            String sessionKey = (String) request.get("sessionKey");

            if (openid == null || encryptedData == null || iv == null || sessionKey == null) {
                return Result.error("参数不完整");
            }

            Map<String, Object> userInfo = wechatService.decryptUserInfo(encryptedData, iv, sessionKey);
            
            // 保存或更新用户信息
            Map<String, Object> result = wechatService.saveUserInfo(openid, userInfo);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 微信手机号授权
     * 获取用户手机号
     */
    @PostMapping("/auth/phone")
    public Result<Map<String, Object>> authorizePhoneNumber(@RequestBody Map<String, Object> request) {
        try {
            String openid = (String) request.get("openid");
            String encryptedData = (String) request.get("encryptedData");
            String iv = (String) request.get("iv");
            String sessionKey = (String) request.get("sessionKey");

            if (openid == null || encryptedData == null || iv == null || sessionKey == null) {
                return Result.error("参数不完整");
            }

            Map<String, Object> phoneInfo = wechatService.decryptPhoneNumber(encryptedData, iv, sessionKey);
            
            // 更新用户手机号
            Map<String, Object> result = wechatService.updateUserPhone(openid, phoneInfo);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取手机号失败: " + e.getMessage());
        }
    }

    /**
     * 通过openid获取用户信息
     */
    @GetMapping("/user/{openid}")
    public Result<Map<String, Object>> getUserByOpenid(@PathVariable String openid) {
        try {
            Map<String, Object> userInfo = wechatService.getUserByOpenid(openid);
            if (userInfo != null) {
                return Result.success(userInfo);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否已完成授权
     */
    @GetMapping("/auth/status/{openid}")
    public Result<Map<String, Object>> getAuthStatus(@PathVariable String openid) {
        try {
            Map<String, Object> authStatus = wechatService.getAuthStatus(openid);
            return Result.success(authStatus);
        } catch (Exception e) {
            return Result.error("获取授权状态失败: " + e.getMessage());
        }
    }

    /**
     * 生成小程序码
     */
    @PostMapping("/qrcode")
    public Result<String> generateQRCode(@RequestBody Map<String, Object> request) {
        try {
            String scene = (String) request.get("scene");
            String page = (String) request.get("page");
            
            String qrCodeUrl = wechatService.generateQRCode(scene, page);
            return Result.success(qrCodeUrl);
        } catch (Exception e) {
            return Result.error("生成小程序码失败: " + e.getMessage());
        }
    }

    /**
     * 发送模板消息
     */
    @PostMapping("/template/send")
    public Result<Boolean> sendTemplateMessage(@RequestBody Map<String, Object> request) {
        try {
            String openid = (String) request.get("openid");
            String templateId = (String) request.get("templateId");
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            String page = (String) request.get("page");

            boolean success = wechatService.sendTemplateMessage(openid, templateId, data, page);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("发送模板消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取微信Access Token（内部使用）
     */
    @GetMapping("/token")
    public Result<String> getAccessToken() {
        try {
            String accessToken = wechatService.getAccessToken();
            return Result.success(accessToken);
        } catch (Exception e) {
            return Result.error("获取Access Token失败: " + e.getMessage());
        }
    }
}