package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LocationMapper {
    
    List<Location> selectAll();
    
    List<Location> selectByType(@Param("type") String type);
    
    Location selectById(@Param("id") Long id);
    
    int insert(Location location);
    
    int updateById(Location location);
    
    int deleteById(@Param("id") Long id);
}