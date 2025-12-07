package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 微信登录
     */
    @PostMapping("/wechat/login")
    public Result<Map<String, Object>> wechatLogin(@RequestBody Map<String, Object> loginData) {
        try {
            Map<String, Object> result = authService.wechatLogin(loginData);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 绑定手机号
     */
    @PostMapping("/wechat/phone")
    public Result<Boolean> bindPhoneNumber(@RequestBody Map<String, String> phoneData) {
        try {
            String token = extractTokenFromRequest();
            boolean success = authService.bindPhoneNumber(token, phoneData);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("绑定手机号失败: " + e.getMessage());
        }
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check")
    public Result<Boolean> checkLoginStatus() {
        try {
            String token = extractTokenFromRequest();
            boolean isValid = authService.checkTokenValid(token);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.error("检查登录状态失败: " + e.getMessage());
        }
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            String newToken = authService.refreshToken(refreshToken);
            return Result.success(newToken);
        } catch (Exception e) {
            return Result.error("刷新token失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Boolean> logout() {
        try {
            String token = extractTokenFromRequest();
            boolean success = authService.logout(token);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("退出登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo() {
        try {
            String token = extractTokenFromRequest();
            Map<String, Object> userInfo = authService.getUserInfoByToken(token);
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中提取token
     */
    private String extractTokenFromRequest() {
        // 这里应该从HTTP Header中获取Authorization token
        // 为了演示，返回一个模拟的token
        return "mock_token_" + System.currentTimeMillis();
    }
}