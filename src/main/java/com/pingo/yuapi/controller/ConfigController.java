package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.dto.ConfigDTO;
import com.pingo.yuapi.entity.AppConfig;
import com.pingo.yuapi.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
@CrossOrigin
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/app")
    public Result<ConfigDTO> getAppConfig() {
        try {
            ConfigDTO config = configService.getAppConfig();
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取应用配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<String> updateConfig(@RequestBody Map<String, String> params) {
        try {
            String key = params.get("key");
            String value = params.get("value");
            
            if (key == null || value == null) {
                return Result.error("参数不完整");
            }
            
            boolean success = configService.updateConfig(key, value);
            if (success) {
                return Result.success("配置更新成功");
            } else {
                return Result.error("配置更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public Result<List<AppConfig>> getAllConfigs() {
        try {
            List<AppConfig> configs = configService.getAllConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("获取配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public Result<List<AppConfig>> getConfigsByType(@PathVariable String type) {
        try {
            List<AppConfig> configs = configService.getConfigsByType(type);
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }
}