package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.TripDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 行程详情数据访问层接口
 */
@Mapper
public interface TripDetailsMapper {

    /**
     * 插入行程详情
     */
    int insertTripDetails(TripDetails details);

    /**
     * 根据tripId查询行程详情
     */
    TripDetails selectByTripId(@Param("tripId") String tripId);

    /**
     * 根据ID查询行程详情
     */
    TripDetails selectById(@Param("id") String id);

    /**
     * 更新行程详情
     */
    int updateTripDetails(TripDetails details);

    /**
     * 根据tripId删除行程详情（级联删除时自动调用）
     */
    int deleteByTripId(@Param("tripId") String tripId);

    /**
     * 根据ID删除行程详情
     */
    int deleteById(@Param("id") String id);
}
