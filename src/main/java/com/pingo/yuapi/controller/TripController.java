package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.Trip;
import com.pingo.yuapi.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    @Autowired
    private TripService tripService;

    /**
     * 获取行程列表
     */
    @GetMapping("/list")
    public Result<List<Trip>> getTripList(@RequestParam Map<String, Object> params) {
        try {
            List<Trip> trips = tripService.getTripList(params);
            return Result.success(trips);
        } catch (Exception e) {
            return Result.error("获取行程列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取今天的行程（距离当前时间后面的）
     */
    @GetMapping("/today")
    public Result<List<Trip>> getTodayTrips(@RequestParam Map<String, Object> params) {
        try {
            // 确保只查询今天的数据
            LocalDate today = LocalDate.now();
            params.put("date", today.toString());
            params.put("afterCurrentTime", LocalDateTime.now());
            
            List<Trip> trips = tripService.getTodayTrips(params);
            return Result.success(trips);
        } catch (Exception e) {
            return Result.error("获取今日行程失败: " + e.getMessage());
        }
    }

    /**
     * 创建行程
     */
    @PostMapping("/create")
    public Result<String> createTrip(@RequestBody Trip trip) {
        try {
            String tripId = tripService.createTrip(trip);
            return Result.success(tripId);
        } catch (Exception e) {
            return Result.error("创建行程失败: " + e.getMessage());
        }
    }

    /**
     * 获取行程详情
     */
    @GetMapping("/{tripId}")
    public Result<Trip> getTripById(@PathVariable String tripId) {
        try {
            Trip trip = tripService.getTripById(tripId);
            if (trip != null) {
                return Result.success(trip);
            } else {
                return Result.error("行程不存在");
            }
        } catch (Exception e) {
            return Result.error("获取行程详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新行程
     */
    @PutMapping("/{tripId}")
    public Result<Boolean> updateTrip(@PathVariable String tripId, @RequestBody Trip trip) {
        try {
            trip.setId(tripId);
            boolean success = tripService.updateTrip(trip);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新行程失败: " + e.getMessage());
        }
    }

    /**
     * 删除行程
     */
    @DeleteMapping("/{tripId}")
    public Result<Boolean> deleteTrip(@PathVariable String tripId) {
        try {
            boolean success = tripService.deleteTrip(tripId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("删除行程失败: " + e.getMessage());
        }
    }

    /**
     * 搜索附近的行程
     */
    @PostMapping("/search/nearby")
    public Result<List<Trip>> searchNearbyTrips(@RequestBody Map<String, Object> searchParams) {
        try {
            List<Trip> trips = tripService.searchNearbyTrips(searchParams);
            return Result.success(trips);
        } catch (Exception e) {
            return Result.error("搜索附近行程失败: " + e.getMessage());
        }
    }
}