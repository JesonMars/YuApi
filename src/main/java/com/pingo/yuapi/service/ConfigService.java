package com.pingo.yuapi.service;

import com.pingo.yuapi.dto.ConfigDTO;
import com.pingo.yuapi.entity.AppConfig;
import com.pingo.yuapi.mapper.AppConfigMapper;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService {

    @Autowired
    private AppConfigMapper appConfigMapper;

    public ConfigDTO getAppConfig() {
        List<AppConfig> configs = appConfigMapper.selectAll();
        ConfigDTO configDTO = new ConfigDTO();
        Map<String, Object> pageTexts = new HashMap<>();
        Map<String, Object> homeButtons = new HashMap<>();
        Map<String, Object> tabBarConfig = new HashMap<>();
        Map<String, Object> notifications = new HashMap<>();

        for (AppConfig config : configs) {
            String key = config.getConfigKey();
            String value = config.getConfigValue();
            
            switch (key) {
                case "app_name":
                    configDTO.setAppName(value);
                    break;
                case "app_slogan":
                    configDTO.setAppSlogan(value);
                    break;
                case "welcome_image":
                    configDTO.setWelcomeImage(value);
                    break;
                case "primary_color":
                    configDTO.setPrimaryColor(value);
                    break;
                case "secondary_color":
                    configDTO.setSecondaryColor(value);
                    break;
                case "home_button_driver":
                    homeButtons.put("driver", value);
                    break;
                case "home_button_passenger":
                    homeButtons.put("passenger", value);
                    break;
                case "tab_bar_square":
                    tabBarConfig.put("square", value);
                    break;
                case "tab_bar_trip":
                    tabBarConfig.put("trip", value);
                    break;
                case "tab_bar_message":
                    tabBarConfig.put("message", value);
                    break;
                case "tab_bar_profile":
                    tabBarConfig.put("profile", value);
                    break;
                case "page_text_welcome_mode_tip":
                    pageTexts.put("welcomeModeTip", value);
                    break;
                case "page_text_commute_route_title":
                    pageTexts.put("commuteRouteTitle", value);
                    break;
                case "page_text_commute_route_desc":
                    pageTexts.put("commuteRouteDesc", value);
                    break;
                case "page_text_publish_trip_title":
                    pageTexts.put("publishTripTitle", value);
                    break;
                case "page_text_waypoint_title":
                    pageTexts.put("waypointTitle", value);
                    break;
                case "page_text_datetime_title":
                    pageTexts.put("datetimeTitle", value);
                    break;
                case "page_text_estimated_income":
                    pageTexts.put("estimatedIncome", value);
                    break;
                case "page_text_publish_button":
                    pageTexts.put("publishButton", value);
                    break;
                case "page_text_waiting_passenger":
                    pageTexts.put("waitingPassenger", value);
                    break;
                case "page_text_passenger_joined":
                    pageTexts.put("passengerJoined", value);
                    break;
                case "page_text_trip_rules":
                    pageTexts.put("tripRules", value);
                    break;
                case "notification_passenger_wait":
                    notifications.put("passengerWait", value);
                    break;
                case "notification_trip_change":
                    notifications.put("tripChange", value);
                    break;
                default:
                    break;
            }
        }
        
        configDTO.setHomeButtons(homeButtons);
        configDTO.setTabBarConfig(tabBarConfig);
        configDTO.setPageTexts(pageTexts);
        configDTO.setNotifications(notifications);
        
        return configDTO;
    }

    public boolean updateConfig(String key, String value) {
        AppConfig config = appConfigMapper.selectByKey(key);
        if (config != null) {
            config.setConfigValue(value);
            return appConfigMapper.updateByKey(config) > 0;
        } else {
            AppConfig newConfig = new AppConfig();
            newConfig.setConfigKey(key);
            newConfig.setConfigValue(value);
            newConfig.setConfigType("app");
            return appConfigMapper.insert(newConfig) > 0;
        }
    }

    public List<AppConfig> getAllConfigs() {
        return appConfigMapper.selectAll();
    }

    public List<AppConfig> getConfigsByType(String type) {
        return appConfigMapper.selectByType(type);
    }
}