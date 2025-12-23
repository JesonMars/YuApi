package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.Vehicle;

import java.util.List;

public interface VehicleService {

    /**
     * 根据ID获取车辆
     */
    Vehicle getVehicleById(String vehicleId);

    /**
     * 根据用户ID获取所有车辆
     */
    List<Vehicle> getVehiclesByUserId(String userId);

    /**
     * 根据用户ID获取默认车辆
     */
    Vehicle getDefaultVehicleByUserId(String userId);

    /**
     * 创建新车辆
     */
    String createVehicle(Vehicle vehicle);

    /**
     * 更新车辆信息
     */
    boolean updateVehicle(Vehicle vehicle);

    /**
     * 设置为默认车辆
     */
    boolean setAsDefaultVehicle(String userId, String vehicleId);

    /**
     * 删除车辆
     */
    boolean deleteVehicle(String vehicleId);

    /**
     * 检查车牌号是否已存在
     */
    boolean checkPlateNumberExists(String plateNumber, String excludeId);

    /**
     * 检查车架号是否已存在
     */
    boolean checkVinExists(String vin, String excludeId);

    /**
     * 从驾驶本扫描并保存车辆信息（未来实现）
     */
    Vehicle scanAndSaveVehicleFromLicense(String userId, String licenseImageUrl);
}
