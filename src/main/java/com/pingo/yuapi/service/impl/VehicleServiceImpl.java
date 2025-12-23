package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.Vehicle;
import com.pingo.yuapi.mapper.VehicleMapper;
import com.pingo.yuapi.service.VehicleService;
import com.pingo.yuapi.utils.IdGeneratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    public Vehicle getVehicleById(String vehicleId) {
        return vehicleMapper.findById(vehicleId);
    }

    @Override
    public List<Vehicle> getVehiclesByUserId(String userId) {
        return vehicleMapper.findByUserId(userId);
    }

    @Override
    public Vehicle getDefaultVehicleByUserId(String userId) {
        Vehicle defaultVehicle = vehicleMapper.findDefaultByUserId(userId);

        // 如果没有默认车辆，返回第一辆车
        if (defaultVehicle == null) {
            List<Vehicle> vehicles = vehicleMapper.findByUserId(userId);
            if (!vehicles.isEmpty()) {
                defaultVehicle = vehicles.get(0);
            }
        }

        return defaultVehicle;
    }

    @Override
    @Transactional
    public String createVehicle(Vehicle vehicle) {
        // 生成ID
        String vehicleId = "VEH-" + IdGeneratorUtils.generateShortId();
        vehicle.setId(vehicleId);

        // 检查车牌号是否已存在
        if (vehicle.getPlateNumber() != null && !vehicle.getPlateNumber().isEmpty()) {
            Vehicle existingVehicle = vehicleMapper.findByPlateNumber(vehicle.getPlateNumber());
            if (existingVehicle != null) {
                throw new RuntimeException("该车牌号已存在");
            }
        }

        // 检查车架号是否已存在
        if (vehicle.getVin() != null && !vehicle.getVin().isEmpty()) {
            Vehicle existingVehicle = vehicleMapper.findByVin(vehicle.getVin());
            if (existingVehicle != null) {
                throw new RuntimeException("该车架号已存在");
            }
        }

        // 如果是用户的第一辆车，自动设为默认
        List<Vehicle> existingVehicles = vehicleMapper.findByUserId(vehicle.getUserId());
        if (existingVehicles.isEmpty()) {
            vehicle.setIsDefault(true);
        }

        // 如果设置为默认车辆，先清除该用户的其他默认车辆
        if (Boolean.TRUE.equals(vehicle.getIsDefault())) {
            vehicleMapper.clearDefaultByUserId(vehicle.getUserId());
        }

        vehicleMapper.createVehicle(vehicle);
        return vehicleId;
    }

    @Override
    @Transactional
    public boolean updateVehicle(Vehicle vehicle) {
        // 检查车辆是否存在
        Vehicle existingVehicle = vehicleMapper.findById(vehicle.getId());
        if (existingVehicle == null) {
            throw new RuntimeException("车辆不存在");
        }

        // 检查车牌号是否已被其他车辆使用
        if (vehicle.getPlateNumber() != null && !vehicle.getPlateNumber().isEmpty()) {
            int count = vehicleMapper.countByPlateNumber(vehicle.getPlateNumber(), vehicle.getId());
            if (count > 0) {
                throw new RuntimeException("该车牌号已被其他车辆使用");
            }
        }

        // 检查车架号是否已被其他车辆使用
        if (vehicle.getVin() != null && !vehicle.getVin().isEmpty()) {
            int count = vehicleMapper.countByVin(vehicle.getVin(), vehicle.getId());
            if (count > 0) {
                throw new RuntimeException("该车架号已被其他车辆使用");
            }
        }

        return vehicleMapper.updateVehicle(vehicle) > 0;
    }

    @Override
    @Transactional
    public boolean setAsDefaultVehicle(String userId, String vehicleId) {
        // 检查车辆是否存在且属于该用户
        Vehicle vehicle = vehicleMapper.findById(vehicleId);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        if (!vehicle.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该车辆");
        }

        // 先清除该用户的所有默认车辆
        vehicleMapper.clearDefaultByUserId(userId);

        // 设置为默认车辆
        return vehicleMapper.setAsDefault(vehicleId) > 0;
    }

    @Override
    @Transactional
    public boolean deleteVehicle(String vehicleId) {
        // 检查车辆是否存在
        Vehicle vehicle = vehicleMapper.findById(vehicleId);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }

        // 删除车辆
        int result = vehicleMapper.deleteVehicle(vehicleId);

        // 如果删除的是默认车辆，将该用户的第一辆车设为默认
        if (Boolean.TRUE.equals(vehicle.getIsDefault())) {
            List<Vehicle> remainingVehicles = vehicleMapper.findByUserId(vehicle.getUserId());
            if (!remainingVehicles.isEmpty()) {
                vehicleMapper.setAsDefault(remainingVehicles.get(0).getId());
            }
        }

        return result > 0;
    }

    @Override
    public boolean checkPlateNumberExists(String plateNumber, String excludeId) {
        if (plateNumber == null || plateNumber.isEmpty()) {
            return false;
        }

        if (excludeId == null) {
            excludeId = "";
        }

        return vehicleMapper.countByPlateNumber(plateNumber, excludeId) > 0;
    }

    @Override
    public boolean checkVinExists(String vin, String excludeId) {
        if (vin == null || vin.isEmpty()) {
            return false;
        }

        if (excludeId == null) {
            excludeId = "";
        }

        return vehicleMapper.countByVin(vin, excludeId) > 0;
    }

    @Override
    public Vehicle scanAndSaveVehicleFromLicense(String userId, String licenseImageUrl) {
        // TODO: 实现驾驶本扫描功能
        // 1. 调用OCR服务识别驾驶本图片
        // 2. 提取车架号、车牌号、品牌等信息
        // 3. 创建并保存车辆信息
        throw new UnsupportedOperationException("驾驶本扫描功能尚未实现");
    }
}
