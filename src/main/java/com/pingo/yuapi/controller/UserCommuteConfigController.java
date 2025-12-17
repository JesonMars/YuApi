package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.UserCommuteConfig;
import com.pingo.yuapi.service.UserCommuteConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/commute-config")
@CrossOrigin(origins = "*")
public class UserCommuteConfigController {

    @Autowired
    private UserCommuteConfigService service;

    @GetMapping
    public Result<UserCommuteConfig> getConfig(@RequestParam String userId, @RequestParam String timing) {
        return Result.success(service.getConfig(userId, timing));
    }

    @PostMapping
    public Result<Boolean> saveConfig(@RequestBody UserCommuteConfig config) {
        return Result.success(service.saveConfig(config));
    }
}
