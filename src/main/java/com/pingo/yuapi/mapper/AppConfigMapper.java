package com.pingo.yuapi.mapper;

import com.pingo.yuapi.entity.AppConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AppConfigMapper {
    
    List<AppConfig> selectAll();
    
    AppConfig selectByKey(@Param("configKey") String configKey);
    
    List<AppConfig> selectByType(@Param("configType") String configType);
    
    int insert(AppConfig appConfig);
    
    int updateByKey(AppConfig appConfig);
    
    int deleteByKey(@Param("configKey") String configKey);
}