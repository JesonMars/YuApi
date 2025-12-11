package com.pingo.yuapi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingo.yuapi.entity.AddressSearchCache;
import com.pingo.yuapi.mapper.AddressSearchCacheMapper;
import com.pingo.yuapi.service.MapApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地图API服务实现
 */
@Service
public class AmapApiServiceImpl implements MapApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(AmapApiServiceImpl.class);
    
    @Value("${amap.api.key:}")
    private String apiKey;
    
    @Value("${amap.api.search.url:https://restapi.amap.com/v3/place/text}")
    private String searchUrl;
    
    @Value("${amap.api.geocode.url:https://restapi.amap.com/v3/geocode/geo}")
    private String geocodeUrl;
    
    @Value("${amap.api.regeo.url:https://restapi.amap.com/v3/geocode/regeo}")
    private String regeoUrl;
    
    @Autowired
    private AddressSearchCacheMapper cacheMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public List<Map<String, Object>> searchAddress(String city, String keyword, boolean useCache) {
        try {
            // 参数验证
            if (keyword == null || keyword.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            // 检查缓存
            if (useCache) {
                AddressSearchCache cached = cacheMapper.selectBySearch(city, keyword.trim(), "amap");
                if (cached != null) {
                    logger.info("从缓存中获取搜索结果: {}, {}", city, keyword);
                    // 更新命中次数
                    cacheMapper.updateHitCount(cached.getId(), cached.getHitCount() + 1);
                    return parseSearchResults(cached.getResults());
                }
            }
            
            // 调用高德API
            List<Map<String, Object>> results = callAmapSearch(city, keyword.trim());
            
            // 保存到缓存
            if (useCache && !results.isEmpty()) {
                try {
                    String resultsJson = objectMapper.writeValueAsString(results);
                    AddressSearchCache cache = new AddressSearchCache();
                    cache.setCityName(city);
                    cache.setKeyword(keyword.trim());
                    cache.setResults(resultsJson);
                    cache.setSource("amap");
                    cache.setHitCount(1);
                    cache.setLastHitTime(LocalDateTime.now());
                    cache.setExpireTime(LocalDateTime.now().plusHours(24)); // 24小时过期
                    cacheMapper.insert(cache);
                    logger.info("搜索结果已缓存: {}, {}", city, keyword);
                } catch (Exception e) {
                    logger.warn("缓存搜索结果失败: {}", e.getMessage());
                }
            }
            
            return results;
            
        } catch (Exception e) {
            logger.error("地址搜索失败: {} - {}: {}", city, keyword, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 调用高德地图搜索API
     */
    private List<Map<String, Object>> callAmapSearch(String city, String keyword) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.warn("高德地图API KEY未配置，返回空结果");
                return new ArrayList<>();
            }
            
            // 构建请求URL
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format("%s?key=%s&keywords=%s&city=%s&output=json&offset=20&page=1",
                    searchUrl, apiKey, encodedKeyword, encodedCity);
            
            logger.info("调用高德API搜索: {}", url);
            
            // 发送请求
            String response = restTemplate.getForObject(url, String.class);
            logger.debug("高德API响应: {}", response);
            
            // 解析响应
            return parseAmapResponse(response);
            
        } catch (Exception e) {
            logger.error("调用高德API失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析高德API响应
     */
    private List<Map<String, Object>> parseAmapResponse(String response) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            
            // 检查响应状态
            String status = rootNode.path("status").asText();
            if (!"1".equals(status)) {
                String info = rootNode.path("info").asText();
                logger.warn("高德API返回错误: {}", info);
                return results;
            }
            
            // 解析POI数据
            JsonNode poisNode = rootNode.path("pois");
            if (poisNode.isArray()) {
                for (JsonNode poiNode : poisNode) {
                    Map<String, Object> poi = new HashMap<>();
                    poi.put("title", poiNode.path("name").asText());
                    poi.put("address", poiNode.path("address").asText());
                    poi.put("type", poiNode.path("type").asText());
                    poi.put("typecode", poiNode.path("typecode").asText());
                    
                    // 解析经纬度
                    String location = poiNode.path("location").asText();
                    if (!location.isEmpty() && location.contains(",")) {
                        String[] coords = location.split(",");
                        if (coords.length == 2) {
                            try {
                                Map<String, Double> locationMap = new HashMap<>();
                                locationMap.put("lng", Double.parseDouble(coords[0]));
                                locationMap.put("lat", Double.parseDouble(coords[1]));
                                poi.put("location", locationMap);
                            } catch (NumberFormatException e) {
                                logger.warn("解析经纬度失败: {}", location);
                            }
                        }
                    }
                    
                    poi.put("adname", poiNode.path("adname").asText()); // 区域名称
                    poi.put("cityname", poiNode.path("cityname").asText()); // 城市名称
                    
                    results.add(poi);
                }
            }
            
            logger.info("解析高德API结果: {} 条记录", results.size());
            
        } catch (Exception e) {
            logger.error("解析高德API响应失败: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * 从缓存JSON解析搜索结果
     */
    private List<Map<String, Object>> parseSearchResults(String resultsJson) {
        try {
            return objectMapper.readValue(resultsJson, List.class);
        } catch (Exception e) {
            logger.error("解析缓存结果失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> geocode(String address, String city) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.warn("高德地图API KEY未配置");
                return new HashMap<>();
            }
            
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format("%s?key=%s&address=%s&city=%s&output=json",
                    geocodeUrl, apiKey, encodedAddress, encodedCity);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            
            if ("1".equals(rootNode.path("status").asText())) {
                JsonNode geocodesNode = rootNode.path("geocodes");
                if (geocodesNode.isArray() && geocodesNode.size() > 0) {
                    JsonNode firstResult = geocodesNode.get(0);
                    Map<String, Object> result = new HashMap<>();
                    
                    String location = firstResult.path("location").asText();
                    if (!location.isEmpty() && location.contains(",")) {
                        String[] coords = location.split(",");
                        result.put("lng", Double.parseDouble(coords[0]));
                        result.put("lat", Double.parseDouble(coords[1]));
                        result.put("formatted_address", firstResult.path("formatted_address").asText());
                        result.put("adcode", firstResult.path("adcode").asText());
                        
                        return result;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("地理编码失败: {}", e.getMessage(), e);
        }
        
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> reverseGeocode(Double lng, Double lat) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.warn("高德地图API KEY未配置");
                return new HashMap<>();
            }
            
            String url = String.format("%s?key=%s&location=%s,%s&output=json&radius=1000&extensions=all",
                    regeoUrl, apiKey, lng, lat);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            
            if ("1".equals(rootNode.path("status").asText())) {
                JsonNode regeocodeNode = rootNode.path("regeocode");
                Map<String, Object> result = new HashMap<>();
                result.put("formatted_address", regeocodeNode.path("formatted_address").asText());
                
                JsonNode addressComponent = regeocodeNode.path("addressComponent");
                result.put("province", addressComponent.path("province").asText());
                result.put("city", addressComponent.path("city").asText());
                result.put("district", addressComponent.path("district").asText());
                result.put("township", addressComponent.path("township").asText());
                
                return result;
            }
            
        } catch (Exception e) {
            logger.error("逆地理编码失败: {}", e.getMessage(), e);
        }
        
        return new HashMap<>();
    }
}