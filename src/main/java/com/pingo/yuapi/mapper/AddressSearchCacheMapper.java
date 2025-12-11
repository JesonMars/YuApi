package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.AddressSearchCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 地址搜索缓存Mapper接口
 */
@Mapper
public interface AddressSearchCacheMapper {
    
    /**
     * 根据城市和关键词获取缓存
     */
    AddressSearchCache selectBySearch(@Param("cityName") String cityName, 
                                     @Param("keyword") String keyword, 
                                     @Param("source") String source);
    
    /**
     * 插入缓存记录
     */
    int insert(AddressSearchCache cache);
    
    /**
     * 更新缓存记录
     */
    int update(AddressSearchCache cache);
    
    /**
     * 更新命中次数和时间
     */
    int updateHitCount(@Param("id") Long id, @Param("hitCount") Integer hitCount);
    
    /**
     * 删除过期缓存
     */
    int deleteExpiredCache();
    
    /**
     * 根据城市和关键词删除缓存
     */
    int deleteBySearch(@Param("cityName") String cityName, 
                      @Param("keyword") String keyword, 
                      @Param("source") String source);
}