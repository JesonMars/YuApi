package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.UserLocation;
import org.apache.ibatis.annotations.*;

/**
 * Mapper for user_locations table
 * Handles CRUD operations for user locations (home, company, etc.)
 */
@Mapper
public interface UserLocationMapper {

        /**
         * Find user location by user ID and type
         * 
         * @param userId User ID
         * @param type   Location type ('home', 'company', 'other')
         * @return UserLocation or null if not found
         */
        @Select("SELECT * FROM user_locations WHERE user_id = #{userId} AND type = #{type}")
        UserLocation findByUserIdAndType(@Param("userId") String userId, @Param("type") String type);

        /**
         * Insert a new user location
         */
        @Insert("INSERT INTO user_locations (id, user_id, name, address, longitude, latitude, type) " +
                        "VALUES (#{id}, #{userId}, #{name}, #{address}, #{longitude}, #{latitude}, #{type})")
        int insert(UserLocation location);

        /**
         * Update an existing user location
         */
        @Update("UPDATE user_locations SET name = #{name}, address = #{address}, " +
                        "longitude = #{longitude}, latitude = #{latitude} WHERE id = #{id}")
        int update(UserLocation location);

        /**
         * Delete user location by ID
         */
        @Delete("DELETE FROM user_locations WHERE id = #{id}")
        int deleteById(@Param("id") String id);
}
