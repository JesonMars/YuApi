package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.UserCommuteConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserCommuteConfigMapper {

        /**
         * 根据用户ID和timing查询配置
         */
        @Select("SELECT * FROM user_commute_config WHERE user_id = #{userId} AND timing = #{timing}")
        UserCommuteConfig findByUserIdAndTiming(@Param("userId") String userId, @Param("timing") String timing);

        /**
         * 插入用户通勤配置（完整版）
         */
        @Insert("INSERT INTO user_commute_config (" +
                        "id, user_id, timing, " +
                        "pickup_points, dropoff_points, " +
                        "default_seat_count, default_price_per_seat, default_recurring, default_recurring_type, default_notes, "
                        +
                        "default_passenger_count, default_offer_price " +
                        ") VALUES (" +
                        "#{id}, #{userId}, #{timing}, " +
                        "#{pickupPoints}, #{dropoffPoints}, " +
                        "#{defaultSeatCount}, #{defaultPricePerSeat}, #{defaultRecurring}, #{defaultRecurringType}, #{defaultNotes}, "
                        +
                        "#{defaultPassengerCount}, #{defaultOfferPrice} " +
                        ")")
        int insert(UserCommuteConfig config);

        /**
         * 更新用户通勤配置（完整版）
         */
        @Update("UPDATE user_commute_config SET " +
                        "pickup_points = #{pickupPoints}, " +
                        "dropoff_points = #{dropoffPoints}, " +
                        "default_seat_count = #{defaultSeatCount}, " +
                        "default_price_per_seat = #{defaultPricePerSeat}, " +
                        "default_recurring = #{defaultRecurring}, " +
                        "default_recurring_type = #{defaultRecurringType}, " +
                        "default_notes = #{defaultNotes}, " +
                        "default_passenger_count = #{defaultPassengerCount}, " +
                        "default_passenger_count = #{defaultPassengerCount}, " +
                        "default_offer_price = #{defaultOfferPrice} " +
                        "WHERE user_id = #{userId} AND timing = #{timing}")
        int update(UserCommuteConfig config);

        /**
         * 根据用户ID和timing删除配置
         */
        @Delete("DELETE FROM user_commute_config WHERE user_id = #{userId} AND timing = #{timing}")
        int deleteByUserIdAndTiming(@Param("userId") String userId, @Param("timing") String timing);

        /**
         * 插入或更新配置（使用ON DUPLICATE KEY UPDATE）
         */
        @Insert("INSERT INTO user_commute_config (" +
                        "id, user_id, timing, " +
                        "pickup_points, dropoff_points, " +
                        "default_seat_count, default_price_per_seat, default_recurring, default_recurring_type, default_notes, "
                        +
                        "default_passenger_count, default_offer_price " +
                        ") VALUES (" +
                        "#{id}, #{userId}, #{timing}, " +
                        "#{pickupPoints}, #{dropoffPoints}, " +
                        "#{defaultSeatCount}, #{defaultPricePerSeat}, #{defaultRecurring}, #{defaultRecurringType}, #{defaultNotes}, "
                        +
                        "#{defaultPassengerCount}, #{defaultOfferPrice} " +
                        ") ON DUPLICATE KEY UPDATE " +
                        "pickup_points = #{pickupPoints}, " +
                        "dropoff_points = #{dropoffPoints}, " +
                        "default_seat_count = #{defaultSeatCount}, " +
                        "default_price_per_seat = #{defaultPricePerSeat}, " +
                        "default_recurring = #{defaultRecurring}, " +
                        "default_recurring_type = #{defaultRecurringType}, " +
                        "default_notes = #{defaultNotes}, " +
                        "default_passenger_count = #{defaultPassengerCount}, " +
                        "default_offer_price = #{defaultOfferPrice}")
        int insertOrUpdate(UserCommuteConfig config);
}
