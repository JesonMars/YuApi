package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.UserCommuteConfig;
import com.pingo.yuapi.mapper.UserCommuteConfigMapper;
import com.pingo.yuapi.service.UserCommuteConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserCommuteConfigServiceImpl implements UserCommuteConfigService {

    private static final Logger logger = LoggerFactory.getLogger(UserCommuteConfigServiceImpl.class);

    private static final String CACHE_PREFIX = "commute:config:";
    private static final long CACHE_EXPIRE_DAYS = 30; // 缓存30天

    @Autowired
    private UserCommuteConfigMapper mapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public UserCommuteConfig getConfig(String userId, String timing) {
        String cacheKey = CACHE_PREFIX + userId + ":" + timing;

        try {
            // 1. 尝试从缓存获取
            if (redisTemplate != null) {
                UserCommuteConfig cached = (UserCommuteConfig) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    logger.debug("从缓存加载途经点配置: userId={}, timing={}", userId, timing);
                    return cached;
                }
            }

            // 2. 缓存未命中，从数据库查询
            UserCommuteConfig config = mapper.findByUserIdAndTiming(userId, timing);

            // 3. 写入缓存
            if (config != null && redisTemplate != null) {
                redisTemplate.opsForValue().set(cacheKey, config, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                logger.debug("途经点配置已写入缓存: userId={}, timing={}", userId, timing);
            }

            return config;

        } catch (Exception e) {
            logger.error("获取途经点配置失败: userId={}, timing={}, error={}", userId, timing, e.getMessage(), e);
            // 缓存异常时降级到数据库查询
            return mapper.findByUserIdAndTiming(userId, timing);
        }
    }

    @Override
    public boolean saveConfig(UserCommuteConfig config) {
        try {
            UserCommuteConfig existing = mapper.findByUserIdAndTiming(config.getUserId(), config.getTiming());
            boolean success;

            if (existing != null) {
                success = mapper.update(config) > 0;
            } else {
                if (config.getId() == null) {
                    config.setId(UUID.randomUUID().toString());
                }
                success = mapper.insert(config) > 0;
            }

            // 保存成功后更新缓存
            if (success) {
                updateCache(config);
            }

            return success;

        } catch (Exception e) {
            logger.error("保存途经点配置失败: userId={}, timing={}, error={}",
                config.getUserId(), config.getTiming(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新缓存
     */
    private void updateCache(UserCommuteConfig config) {
        if (redisTemplate == null) {
            return;
        }

        try {
            String cacheKey = CACHE_PREFIX + config.getUserId() + ":" + config.getTiming();

            // 先查询完整数据（包含自动设置的时间戳）
            UserCommuteConfig fullConfig = mapper.findByUserIdAndTiming(config.getUserId(), config.getTiming());

            if (fullConfig != null) {
                redisTemplate.opsForValue().set(cacheKey, fullConfig, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                logger.info("途经点配置缓存已更新: userId={}, timing={}", config.getUserId(), config.getTiming());
            }

        } catch (Exception e) {
            logger.error("更新途经点配置缓存失败: userId={}, timing={}, error={}",
                config.getUserId(), config.getTiming(), e.getMessage(), e);
            // 缓存更新失败不影响主流程
        }
    }
}
