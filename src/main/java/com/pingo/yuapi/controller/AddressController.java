package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.service.MapApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 地址搜索相关接口
 */
@CrossOrigin
@RestController
@RequestMapping("/address")
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private MapApiService mapApiService;

    /**
     * 根据城市和关键词搜索地址
     */
    @PostMapping("/search")
    public Result<List<Map<String, Object>>> searchAddressByCity(@RequestBody Map<String, String> request) {
        String city = request.get("city");
        String keyword = request.get("keyword");

        logger.info("收到地址搜索请求 - 城市: {}, 关键词: {}", city, keyword);

        try {
            // 参数验证
            if (city == null || city.trim().isEmpty()) {
                logger.warn("城市参数为空");
                return Result.error("城市参数不能为空");
            }

            if (keyword == null || keyword.trim().isEmpty()) {
                logger.warn("搜索关键词为空");
                return Result.error("搜索关键词不能为空");
            }

            // 调用地图API进行搜索，使用缓存
            List<Map<String, Object>> results = mapApiService.searchAddress(city, keyword, false);

            logger.info("返回搜索结果数量: {}", results.size());
            return Result.success(results);

        } catch (Exception e) {
            logger.error("地址搜索异常: {}", e.getMessage(), e);
            return Result.error("地址搜索失败: " + e.getMessage());
        }
    }

    /**
     * 地理编码 - 将地址转换为经纬度
     */
    @PostMapping("/geocode")
    public Result<Map<String, Object>> geocode(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        String city = request.get("city");

        logger.info("地理编码请求 - 地址: {}, 城市: {}", address, city);

        try {
            Map<String, Object> result = mapApiService.geocode(address, city);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("地理编码失败: {}", e.getMessage(), e);
            return Result.error("地理编码失败: " + e.getMessage());
        }
    }

    /**
     * 逆地理编码 - 将经纬度转换为地址
     */
    @PostMapping("/reverse-geocode")
    public Result<Map<String, Object>> reverseGeocode(@RequestBody Map<String, Object> request) {
        try {
            Double lng = Double.valueOf(request.get("lng").toString());
            Double lat = Double.valueOf(request.get("lat").toString());

            logger.info("逆地理编码请求 - 经度: {}, 纬度: {}", lng, lat);

            Map<String, Object> result = mapApiService.reverseGeocode(lng, lat);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("逆地理编码失败: {}", e.getMessage(), e);
            return Result.error("逆地理编码失败: " + e.getMessage());
        }
    }
}