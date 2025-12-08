package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.WechatUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WechatUserMapper {

    /**
     * 根据openid查询微信用户
     */
    @Select("SELECT * FROM wechat_users WHERE openid = #{openid}")
    WechatUser findByOpenid(@Param("openid") String openid);

    /**
     * 创建微信用户记录
     */
    @Insert("INSERT INTO wechat_users (id, openid, unionid, session_key, nick_name, avatar_url, " +
            "gender, city, province, country, language, phone_number, create_time, update_time) " +
            "VALUES (#{id}, #{openid}, #{unionid}, #{sessionKey}, #{nickName}, #{avatarUrl}, " +
            "#{gender}, #{city}, #{province}, #{country}, #{language}, #{phoneNumber}, NOW(), NOW())")
    int createWechatUser(WechatUser wechatUser);

    /**
     * 更新微信用户信息
     */
    @Update("UPDATE wechat_users SET nick_name = #{nickName}, avatar_url = #{avatarUrl}, " +
            "gender = #{gender}, city = #{city}, province = #{province}, country = #{country}, " +
            "language = #{language}, update_time = NOW() WHERE openid = #{openid}")
    int updateUserInfo(WechatUser wechatUser);

    /**
     * 更新手机号
     */
    @Update("UPDATE wechat_users SET phone_number = #{phoneNumber}, update_time = NOW() WHERE openid = #{openid}")
    int updatePhoneNumber(@Param("openid") String openid, @Param("phoneNumber") String phoneNumber);

    /**
     * 更新session_key
     */
    @Update("UPDATE wechat_users SET session_key = #{sessionKey}, update_time = NOW() WHERE openid = #{openid}")
    int updateSessionKey(@Param("openid") String openid, @Param("sessionKey") String sessionKey);

    /**
     * 删除微信用户记录
     */
    @Delete("DELETE FROM wechat_users WHERE openid = #{openid}")
    int deleteByOpenid(@Param("openid") String openid);
}