# 高德地图API配置指南

## 1. 获取高德地图API Key

1. 访问高德开放平台：https://console.amap.com/
2. 注册并登录账号
3. 创建新应用，选择"Web服务API"类型
4. 记录生成的API Key

## 2. 配置API Key

在 `application.yml` 文件中替换 `YOUR_AMAP_API_KEY`：

```yaml
# 高德地图配置
amap:
  api:
    key: 你的真实API密钥  # 替换为从高德开放平台获取的API Key
    search:
      url: https://restapi.amap.com/v3/place/text  # POI搜索API
    geocode:
      url: https://restapi.amap.com/v3/geocode/geo  # 地理编码API
    regeo:
      url: https://restapi.amap.com/v3/geocode/regeo  # 逆地理编码API
```

## 3. API功能说明

### 3.1 地址搜索 (POI搜索)
- **URL**: `/address/search`
- **方法**: POST
- **参数**: `{"city": "城市名", "keyword": "搜索关键词"}`
- **功能**: 在指定城市内搜索地址/地点

### 3.2 地理编码
- **URL**: `/address/geocode`
- **方法**: POST
- **参数**: `{"address": "地址", "city": "城市名"}`
- **功能**: 将地址转换为经纬度坐标

### 3.3 逆地理编码
- **URL**: `/address/reverse-geocode`
- **方法**: POST
- **参数**: `{"lng": 经度, "lat": 纬度}`
- **功能**: 将经纬度坐标转换为地址信息

## 4. 缓存机制

系统实现了智能缓存机制：
- 搜索结果自动缓存24小时
- 相同查询会直接返回缓存结果，提高响应速度
- 缓存命中次数统计，用于优化热点数据

## 5. 城市数据管理

城市数据存储在数据库 `cities` 表中，包含：
- 50个主要城市
- 城市中心经纬度坐标
- 城市编码和行政区划代码
- 可通过管理接口动态添加新城市

## 6. 错误处理

- API Key未配置：返回空结果，记录警告日志
- 网络异常：记录错误日志，返回空结果
- 参数验证失败：记录警告日志，返回空结果

## 7. 安全注意事项

1. **API Key保护**：
   - 不要将API Key提交到版本控制系统
   - 使用环境变量或配置文件管理敏感信息

2. **访问频率限制**：
   - 高德API有访问频率限制
   - 合理使用缓存机制减少API调用次数

3. **数据使用规范**：
   - 遵守高德地图服务条款
   - 不滥用API服务

## 8. 监控和日志

系统记录详细的日志信息：
- API调用成功/失败次数
- 缓存命中率
- 响应时间统计
- 错误原因分析

通过日志可以监控地图服务的运行状况和性能表现。