package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.Trip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 行程数据访问层接口
 */
@Mapper
public interface TripMapper {
    
    /**
     * 查询所有行程，按出发时间排序
     */
    List<Trip> selectAllTrips();
    
    /**
     * 根据条件查询行程
     */
    List<Trip> selectTripsByCondition(Map<String, Object> params);
    
    /**
     * 查询今天的可用行程（时间在当前时间之后）
     */
    List<Trip> selectTodayTrips(@Param("now") LocalDateTime now);
    
    /**
     * 根据日期查询行程
     */
    List<Trip> selectTripsByDate(@Param("targetDate") LocalDate targetDate);
    
    /**
     * 根据ID查询行程
     */
    Trip selectTripById(@Param("id") String id);
    
    /**
     * 插入行程
     */
    int insertTrip(Trip trip);
    
    /**
     * 更新行程
     */
    int updateTrip(Trip trip);
    
    /**
     * 删除行程
     */
    int deleteTripById(@Param("id") String id);
    
    /**
     * 查询用户的行程
     */
    List<Trip> selectUserTrips(@Param("userId") String userId, 
                               @Param("offset") int offset, 
                               @Param("limit") int limit);
    
    /**
     * 根据状态查询行程数量
     */
    int countTripsByStatus(@Param("status") String status);
    
    /**
     * 更新行程状态
     */
    int updateTripStatus(@Param("id") String id, @Param("status") String status);
    
    /**
     * 减少可用座位数
     */
    int decreaseAvailableSeats(@Param("id") String id, @Param("count") int count);
}