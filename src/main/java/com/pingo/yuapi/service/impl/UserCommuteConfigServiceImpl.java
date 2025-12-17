package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.UserCommuteConfig;
import com.pingo.yuapi.mapper.UserCommuteConfigMapper;
import com.pingo.yuapi.service.UserCommuteConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserCommuteConfigServiceImpl implements UserCommuteConfigService {

    @Autowired
    private UserCommuteConfigMapper mapper;

    @Override
    public UserCommuteConfig getConfig(String userId, String timing) {
        return mapper.findByUserIdAndTiming(userId, timing);
    }

    @Override
    public boolean saveConfig(UserCommuteConfig config) {
        UserCommuteConfig existing = mapper.findByUserIdAndTiming(config.getUserId(), config.getTiming());
        if (existing != null) {
            return mapper.update(config) > 0;
        } else {
            if (config.getId() == null) {
                config.setId(UUID.randomUUID().toString());
            }
            return mapper.insert(config) > 0;
        }
    }
}
