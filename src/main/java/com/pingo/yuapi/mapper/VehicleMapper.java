package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.Vehicle;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VehicleMapper {

    /**
     * 根据ID查询车辆
     */
    @Select("SELECT * FROM vehicles WHERE id = #{id}")
    Vehicle findById(@Param("id") String id);

    /**
     * 根据用户ID查询所有车辆
     */
    @Select("SELECT * FROM vehicles WHERE user_id = #{userId} ORDER BY is_default DESC, create_time DESC")
    List<Vehicle> findByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID查询默认车辆
     */
    @Select("SELECT * FROM vehicles WHERE user_id = #{userId} AND is_default = TRUE LIMIT 1")
    Vehicle findDefaultByUserId(@Param("userId") String userId);

    /**
     * 根据车牌号查询车辆
     */
    @Select("SELECT * FROM vehicles WHERE plate_number = #{plateNumber}")
    Vehicle findByPlateNumber(@Param("plateNumber") String plateNumber);

    /**
     * 根据车架号查询车辆
     */
    @Select("SELECT * FROM vehicles WHERE vin = #{vin}")
    Vehicle findByVin(@Param("vin") String vin);

    /**
     * 创建新车辆
     */
    @Insert("INSERT INTO vehicles (id, user_id, vin, plate_number, brand, color, model, is_default) " +
            "VALUES (#{id}, #{userId}, #{vin}, #{plateNumber}, #{brand}, #{color}, #{model}, #{isDefault})")
    int createVehicle(Vehicle vehicle);

    /**
     * 更新车辆信息
     */
    @Update("UPDATE vehicles SET vin = #{vin}, plate_number = #{plateNumber}, brand = #{brand}, " +
            "color = #{color}, model = #{model} WHERE id = #{id}")
    int updateVehicle(Vehicle vehicle);

    /**
     * 设置为默认车辆（先将该用户的所有车辆设为非默认）
     */
    @Update("UPDATE vehicles SET is_default = FALSE WHERE user_id = #{userId}")
    int clearDefaultByUserId(@Param("userId") String userId);

    /**
     * 设置为默认车辆
     */
    @Update("UPDATE vehicles SET is_default = TRUE WHERE id = #{id}")
    int setAsDefault(@Param("id") String id);

    /**
     * 删除车辆
     */
    @Delete("DELETE FROM vehicles WHERE id = #{id}")
    int deleteVehicle(@Param("id") String id);

    /**
     * 检查车牌号是否已存在（排除指定ID）
     */
    @Select("SELECT COUNT(*) FROM vehicles WHERE plate_number = #{plateNumber} AND id != #{excludeId}")
    int countByPlateNumber(@Param("plateNumber") String plateNumber, @Param("excludeId") String excludeId);

    /**
     * 检查车架号是否已存在（排除指定ID）
     */
    @Select("SELECT COUNT(*) FROM vehicles WHERE vin = #{vin} AND id != #{excludeId}")
    int countByVin(@Param("vin") String vin, @Param("excludeId") String excludeId);
}
