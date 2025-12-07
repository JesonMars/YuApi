package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.Location;
import com.pingo.yuapi.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/location")
@CrossOrigin
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/all")
    public Result<List<Location>> getAllLocations() {
        try {
            List<Location> locations = locationService.getAllLocations();
            return Result.success(locations);
        } catch (Exception e) {
            return Result.error("获取地点列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public Result<List<Location>> getLocationsByType(@PathVariable String type) {
        try {
            List<Location> locations = locationService.getLocationsByType(type);
            return Result.success(locations);
        } catch (Exception e) {
            return Result.error("获取地点失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Location> getLocationById(@PathVariable Long id) {
        try {
            Location location = locationService.getLocationById(id);
            if (location != null) {
                return Result.success(location);
            } else {
                return Result.error("地点不存在");
            }
        } catch (Exception e) {
            return Result.error("获取地点信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<String> addLocation(@RequestBody Location location) {
        try {
            boolean success = locationService.addLocation(location);
            if (success) {
                return Result.success("添加地点成功");
            } else {
                return Result.error("添加地点失败");
            }
        } catch (Exception e) {
            return Result.error("添加地点失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<String> updateLocation(@RequestBody Location location) {
        try {
            boolean success = locationService.updateLocation(location);
            if (success) {
                return Result.success("更新地点成功");
            } else {
                return Result.error("更新地点失败");
            }
        } catch (Exception e) {
            return Result.error("更新地点失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteLocation(@PathVariable Long id) {
        try {
            boolean success = locationService.deleteLocation(id);
            if (success) {
                return Result.success("删除地点成功");
            } else {
                return Result.error("删除地点失败");
            }
        } catch (Exception e) {
            return Result.error("删除地点失败: " + e.getMessage());
        }
    }
}