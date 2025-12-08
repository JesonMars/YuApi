package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     */
    @Select("SELECT * FROM users WHERE wechat_openid = #{openid}")
    User findByWechatOpenid(@Param("openid") String openid);

    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(@Param("id") String id);

    /**
     * 创建新用户
     */
    @Insert("INSERT INTO users (id, name, phone, avatar, community, wechat_openid, wechat_unionid, create_time, update_time) " +
            "VALUES (#{id}, #{name}, #{phone}, #{avatar}, #{community}, #{wechatOpenid}, #{wechatUnionid}, NOW(), NOW())")
    int createUser(User user);

    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET name = #{name}, phone = #{phone}, avatar = #{avatar}, " +
            "community = #{community}, update_time = NOW() WHERE id = #{id}")
    int updateUser(User user);

    /**
     * 更新用户头像
     */
    @Update("UPDATE users SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") String id, @Param("avatar") String avatar);

    /**
     * 更新用户手机号
     */
    @Update("UPDATE users SET phone = #{phone}, update_time = NOW() WHERE id = #{id}")
    int updatePhone(@Param("id") String id, @Param("phone") String phone);

    /**
     * 更新用户昵称
     */
    @Update("UPDATE users SET name = #{name}, update_time = NOW() WHERE id = #{id}")
    int updateName(@Param("id") String id, @Param("name") String name);

    /**
     * 更新车辆信息
     */
    @Update("UPDATE users SET vehicle_brand = #{vehicleBrand}, vehicle_color = #{vehicleColor}, " +
            "plate_number = #{plateNumber}, update_time = NOW() WHERE id = #{id}")
    int updateVehicleInfo(@Param("id") String id, @Param("vehicleBrand") String vehicleBrand, 
                         @Param("vehicleColor") String vehicleColor, @Param("plateNumber") String plateNumber);

    /**
     * 更新认证状态
     */
    @Update("UPDATE users SET verification_status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateVerificationStatus(@Param("id") String id, @Param("status") String status);

    /**
     * 更新首次设置完成状态
     */
    @Update("UPDATE users SET first_setup_completed = #{completed}, update_time = NOW() WHERE wechat_openid = #{openid}")
    int updateFirstSetupCompleted(@Param("openid") String openid, @Param("completed") boolean completed);

    /**
     * 删除用户
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteUser(@Param("id") String id);
}