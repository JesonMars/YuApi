package com.pingo.yuapi.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址搜索相关接口
 */
@CrossOrigin
@RestController
@RequestMapping("/address")
public class AddressController {

    /**
     * 根据城市和关键词搜索地址
     */
    @PostMapping("/search")
    public List<Map<String, Object>> searchAddressByCity(@RequestBody Map<String, String> request) {
        String city = request.get("city");
        String keyword = request.get("keyword");
        
        System.out.println("收到地址搜索请求 - 城市: " + city + ", 关键词: " + keyword);
        
        // TODO: 这里应该调用真实的地图API进行地址搜索
        // 暂时返回模拟数据
        List<Map<String, Object>> results = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 生成模拟搜索结果
            String[] types = {"大厦", "商务中心", "小区", "地铁站", "写字楼", "公园", "商场", "医院"};
            
            for (int i = 0; i < Math.min(6, types.length); i++) {
                Map<String, Object> result = new HashMap<>();
                result.put("title", keyword + types[i]);
                result.put("address", city.replace("市", "") + "市" + getRandomDistrict() + keyword + types[i]);
                
                Map<String, Double> location = new HashMap<>();
                location.put("lat", 39.9042 + (Math.random() - 0.5) * 0.1);
                location.put("lng", 116.4074 + (Math.random() - 0.5) * 0.1);
                result.put("location", location);
                
                results.add(result);
            }
            
            System.out.println("返回搜索结果数量: " + results.size());
        } else {
            System.out.println("关键词为空，返回空结果");
        }
        
        return results;
    }
    
    /**
     * 获取随机区域名称（模拟数据）
     */
    private String getRandomDistrict() {
        String[] districts = {"朝阳区", "海淀区", "丰台区", "西城区", "东城区", "石景山区", "通州区", "昌平区"};
        return districts[(int) (Math.random() * districts.length)];
    }
}