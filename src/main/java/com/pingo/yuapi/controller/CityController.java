package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.City;
import com.pingo.yuapi.mapper.CityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 城市管理相关接口
 */
@CrossOrigin
@RestController
@RequestMapping("/cities")
public class CityController {

    private static final Logger logger = LoggerFactory.getLogger(CityController.class);
    
    @Autowired
    private CityMapper cityMapper;

    /**
     * 获取已开通城市列表
     */
    @GetMapping("/available")
    public Result<List<String>> getAvailableCities() {
        try {
            logger.info("获取已开通城市列表");
            List<City> cities = cityMapper.selectEnabledCities();
            List<String> cityNames = cities.stream()
                    .map(City::getCityName)
                    .collect(Collectors.toList());
            return Result.success(cityNames);
        } catch (Exception e) {
            logger.error("获取城市列表失败: {}", e.getMessage(), e);
            return Result.error("获取城市列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索城市
     */
    @GetMapping("/search")
    public Result<List<City>> searchCities(@RequestParam String keyword) {
        try {
            logger.info("搜索城市: {}", keyword);
            List<City> cities = cityMapper.selectCitiesByName(keyword);
            return Result.success(cities);
        } catch (Exception e) {
            logger.error("搜索城市失败: {}", e.getMessage(), e);
            return Result.error("搜索城市失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据名称获取城市详细信息
     */
    @GetMapping("/{cityName}")
    public Result<City> getCityByName(@PathVariable String cityName) {
        try {
            logger.info("获取城市详细信息: {}", cityName);
            City city = cityMapper.selectByCityName(cityName);
            return Result.success(city);
        } catch (Exception e) {
            logger.error("获取城市信息失败: {}", e.getMessage(), e);
            return Result.error("获取城市信息失败: " + e.getMessage());
        }
    }
}