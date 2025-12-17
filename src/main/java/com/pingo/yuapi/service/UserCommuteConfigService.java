package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.UserCommuteConfig;

public interface UserCommuteConfigService {
    UserCommuteConfig getConfig(String userId, String timing);

    boolean saveConfig(UserCommuteConfig config);
}
