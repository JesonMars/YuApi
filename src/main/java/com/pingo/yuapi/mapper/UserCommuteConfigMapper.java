package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.UserCommuteConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserCommuteConfigMapper {
        @Select("SELECT * FROM user_commute_config WHERE user_id = #{userId} AND timing = #{timing}")
        UserCommuteConfig findByUserIdAndTiming(@Param("userId") String userId, @Param("timing") String timing);

        @Insert("INSERT INTO user_commute_config (id, user_id, timing, pickup_points, dropoff_points) "
                        +
                        "VALUES (#{id}, #{userId}, #{timing}, #{pickupPoints}, #{dropoffPoints})")
        int insert(UserCommuteConfig config);

        @Update("UPDATE user_commute_config SET pickup_points = #{pickupPoints}, dropoff_points = #{dropoffPoints} "
                        +
                        "WHERE user_id = #{userId} AND timing = #{timing}")
        int update(UserCommuteConfig config);
}
