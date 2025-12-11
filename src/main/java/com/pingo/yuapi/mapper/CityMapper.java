package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 城市Mapper接口
 */
@Mapper
public interface CityMapper {
    
    /**
     * 获取所有已启用的城市列表（按排序权重排序）
     */
    List<City> selectEnabledCities();
    
    /**
     * 根据城市名称搜索城市
     */
    List<City> selectCitiesByName(@Param("cityName") String cityName);
    
    /**
     * 根据ID获取城市信息
     */
    City selectById(@Param("id") Long id);
    
    /**
     * 根据城市名称获取城市信息
     */
    City selectByCityName(@Param("cityName") String cityName);
    
    /**
     * 插入城市
     */
    int insert(City city);
    
    /**
     * 更新城市
     */
    int update(City city);
    
    /**
     * 删除城市
     */
    int deleteById(@Param("id") Long id);
}