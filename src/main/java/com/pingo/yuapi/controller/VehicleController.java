package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.Vehicle;
import com.pingo.yuapi.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    /**
     * 获取用户的所有车辆
     */
    @GetMapping("/user/{userId}")
    public Result<List<Vehicle>> getUserVehicles(@PathVariable String userId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByUserId(userId);
        return Result.success(vehicles);
    }

    /**
     * 获取用户的默认车辆
     */
    @GetMapping("/user/{userId}/default")
    public Result<Vehicle> getDefaultVehicle(@PathVariable String userId) {
        Vehicle vehicle = vehicleService.getDefaultVehicleByUserId(userId);
        return Result.success(vehicle);
    }

    /**
     * 根据ID获取车辆详情
     */
    @GetMapping("/{vehicleId}")
    public Result<Vehicle> getVehicleById(@PathVariable String vehicleId) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        return Result.success(vehicle);
    }

    /**
     * 创建新车辆
     */
    @PostMapping
    public Result<String> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            String vehicleId = vehicleService.createVehicle(vehicle);
            return Result.success(vehicleId);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新车辆信息
     */
    @PutMapping("/{vehicleId}")
    public Result<Boolean> updateVehicle(@PathVariable String vehicleId, @RequestBody Vehicle vehicle) {
        try {
            vehicle.setId(vehicleId);
            boolean success = vehicleService.updateVehicle(vehicle);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置为默认车辆
     */
    @PutMapping("/{vehicleId}/set-default")
    public Result<Boolean> setAsDefault(@PathVariable String vehicleId, @RequestParam String userId) {
        try {
            boolean success = vehicleService.setAsDefaultVehicle(userId, vehicleId);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{vehicleId}")
    public Result<Boolean> deleteVehicle(@PathVariable String vehicleId) {
        try {
            boolean success = vehicleService.deleteVehicle(vehicleId);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查车牌号是否已存在
     */
    @GetMapping("/check-plate")
    public Result<Boolean> checkPlateNumberExists(@RequestParam String plateNumber,
                                                   @RequestParam(required = false) String excludeId) {
        boolean exists = vehicleService.checkPlateNumberExists(plateNumber, excludeId);
        return Result.success(exists);
    }

    /**
     * 检查车架号是否已存在
     */
    @GetMapping("/check-vin")
    public Result<Boolean> checkVinExists(@RequestParam String vin,
                                           @RequestParam(required = false) String excludeId) {
        boolean exists = vehicleService.checkVinExists(vin, excludeId);
        return Result.success(exists);
    }

    /**
     * 扫描驾驶本并保存车辆信息（未来实现）
     */
    @PostMapping("/scan-license")
    public Result<Vehicle> scanAndSaveVehicle(@RequestParam String userId,
                                               @RequestParam String licenseImageUrl) {
        try {
            Vehicle vehicle = vehicleService.scanAndSaveVehicleFromLicense(userId, licenseImageUrl);
            return Result.success(vehicle);
        } catch (UnsupportedOperationException e) {
            return Result.error("驾驶本扫描功能尚未实现");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
