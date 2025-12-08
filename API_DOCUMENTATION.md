# YuApi Backend API Documentation

## 概述
YuGo拼车应用的后端API接口文档，提供了前端应用所需的所有接口功能，包括微信小程序授权登录。

## 基础信息
- Base URL: `http://localhost:8081/api`
- 数据格式: JSON
- 返回格式统一为 Result<T> 格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

## 微信小程序相关接口 (WechatController)

### 1. 微信登录授权

#### 微信小程序登录
- **URL**: `POST /api/wechat/login`
- **描述**: 通过微信小程序的code换取openid和session_key
- **参数**: 
```json
{
  "code": "微信小程序wx.login()获取的code"
}
```
- **返回**: 
```json
{
  "openid": "用户openid",
  "sessionKey": "会话密钥", 
  "unionid": "用户unionid",
  "isNewUser": true,
  "userInfo": {} // 如果是老用户则返回用户信息
}
```

#### 微信用户信息授权
- **URL**: `POST /api/wechat/auth/userinfo`
- **描述**: 解密并保存用户微信信息（头像、昵称等）
- **参数**: 
```json
{
  "openid": "用户openid",
  "encryptedData": "加密的用户数据",
  "iv": "初始向量",
  "sessionKey": "会话密钥"
}
```
- **返回**: 
```json
{
  "userId": "内部用户ID",
  "userInfo": {用户信息},
  "success": true
}
```

#### 微信手机号授权
- **URL**: `POST /api/wechat/auth/phone`
- **描述**: 解密并保存用户手机号
- **参数**: 
```json
{
  "openid": "用户openid",
  "encryptedData": "加密的手机号数据",
  "iv": "初始向量",
  "sessionKey": "会话密钥"
}
```
- **返回**: 
```json
{
  "phoneNumber": "手机号码",
  "success": true
}
```

### 2. 用户信息管理

#### 根据openid获取用户信息
- **URL**: `GET /api/wechat/user/{openid}`
- **描述**: 通过openid获取用户的完整信息
- **返回**: 用户信息和微信信息的组合

#### 获取用户授权状态
- **URL**: `GET /api/wechat/auth/status/{openid}`
- **描述**: 检查用户是否已完成微信授权
- **返回**: 
```json
{
  "openid": "用户openid",
  "hasUserInfo": true,
  "hasPhone": true,
  "nickName": "用户昵称",
  "avatarUrl": "头像URL"
}
```

### 3. 微信功能扩展

#### 生成小程序码
- **URL**: `POST /api/wechat/qrcode`
- **描述**: 生成小程序二维码
- **参数**: 
```json
{
  "scene": "场景值",
  "page": "页面路径"
}
```
- **返回**: 二维码URL

#### 发送模板消息
- **URL**: `POST /api/wechat/template/send`
- **描述**: 发送微信模板消息通知
- **参数**: 
```json
{
  "openid": "用户openid",
  "templateId": "模板ID",
  "data": {模板数据},
  "page": "跳转页面"
}
```
- **返回**: Boolean

#### 获取Access Token
- **URL**: `GET /api/wechat/token`
- **描述**: 获取微信API调用凭证（内部使用）
- **返回**: Access Token字符串

---

## 用户相关接口 (UserController)

### 1. 用户资料管理

#### 获取用户资料
- **URL**: `GET /api/user/profile`
- **描述**: 获取当前用户的详细资料信息
- **返回**: User对象

#### 更新用户资料
- **URL**: `PUT /api/user/profile`
- **描述**: 更新用户基本资料
- **参数**: User对象
- **返回**: Boolean

#### 更新用户昵称
- **URL**: `PUT /api/user/nickname`
- **描述**: 单独更新用户昵称
- **参数**: 
```json
{
  "nickname": "新昵称"
}
```
- **返回**: Boolean

### 2. 用户位置管理

#### 获取用户位置列表
- **URL**: `GET /api/user/locations`
- **描述**: 获取用户保存的常用位置
- **返回**: List<Map<String, Object>>

#### 保存用户位置
- **URL**: `POST /api/user/locations`
- **描述**: 保存新的常用位置
- **参数**: 位置信息对象
- **返回**: 位置ID (String)

### 3. 关注功能

#### 获取关注列表
- **URL**: `GET /api/user/follows`
- **描述**: 获取用户关注的司机列表
- **返回**: List<Map<String, Object>>

#### 关注司机
- **URL**: `POST /api/user/follow`
- **描述**: 关注指定司机
- **参数**: 
```json
{
  "driverId": "司机ID"
}
```
- **返回**: Boolean

#### 取消关注
- **URL**: `DELETE /api/user/unfollow`
- **描述**: 取消关注指定司机
- **参数**: 
```json
{
  "driverId": "司机ID"
}
```
- **返回**: Boolean

### 4. 黑名单管理

#### 获取黑名单
- **URL**: `GET /api/user/blacklist`
- **描述**: 获取用户的黑名单列表
- **返回**: List<Map<String, Object>>

#### 添加黑名单
- **URL**: `POST /api/user/blacklist`
- **描述**: 将用户添加到黑名单
- **参数**: 
```json
{
  "userId": "目标用户ID",
  "reason": "添加原因"
}
```
- **返回**: Boolean

#### 移除黑名单
- **URL**: `DELETE /api/user/blacklist`
- **描述**: 从黑名单中移除用户
- **参数**: 
```json
{
  "userId": "目标用户ID"
}
```
- **返回**: Boolean

### 5. 车主认证

#### 提交车主认证
- **URL**: `POST /api/user/verification`
- **描述**: 提交车主认证材料
- **参数**: 认证数据对象
- **返回**: Boolean

#### 获取认证状态
- **URL**: `GET /api/user/verification-status`
- **描述**: 获取用户认证状态
- **返回**: Map<String, Object> (包含状态、提交时间、审核信息等)

#### 上传认证文件
- **URL**: `POST /api/user/verification/upload`
- **描述**: 上传认证相关文件
- **参数**: 
  - file: MultipartFile
  - type: String (文件类型)
- **返回**: 文件URL (String)

### 6. 文件上传

#### 上传头像
- **URL**: `POST /api/user/avatar`
- **描述**: 上传用户头像
- **参数**: 
  - avatar: MultipartFile
- **返回**: 头像URL (String)

### 7. 用户统计

#### 获取用户统计信息
- **URL**: `GET /api/user/stats`
- **描述**: 获取用户的统计数据（出行次数、节省金额、减碳量等）
- **返回**: 
```json
{
  "trips": 128,
  "savedMoney": 1580.5,
  "carbon": 45.2
}
```

### 8. 用户设置

#### 获取用户设置
- **URL**: `GET /api/user/settings`
- **描述**: 获取用户的应用设置
- **返回**: Map<String, Object>

#### 更新用户设置
- **URL**: `PUT /api/user/settings`
- **描述**: 更新用户设置
- **参数**: 设置对象
- **返回**: Boolean

### 9. 钱包功能

#### 获取钱包信息
- **URL**: `GET /api/user/wallet`
- **描述**: 获取用户钱包余额和相关信息
- **返回**: 
```json
{
  "balance": 150.50,
  "frozenAmount": 0.00,
  "coupons": 3,
  "points": 1250
}
```

#### 钱包充值
- **URL**: `POST /api/user/wallet/recharge`
- **描述**: 钱包充值
- **参数**: 
```json
{
  "amount": 100.00,
  "paymentMethod": "wechat"
}
```
- **返回**: Boolean

#### 钱包提现
- **URL**: `POST /api/user/wallet/withdraw`
- **描述**: 钱包提现
- **参数**: 
```json
{
  "amount": 50.00,
  "bankAccount": "银行账户信息"
}
```
- **返回**: Boolean

#### 获取交易记录
- **URL**: `GET /api/user/wallet/transactions?page=1&limit=20`
- **描述**: 获取钱包交易记录
- **参数**: 
  - page: 页码 (默认1)
  - limit: 每页条数 (默认20)
- **返回**: List<Map<String, Object>>

### 10. 验证功能

#### 检查手机号
- **URL**: `GET /api/user/check-phone/{phone}`
- **描述**: 检查手机号是否已存在
- **返回**: Boolean

#### 发送验证码
- **URL**: `POST /api/user/send-sms`
- **描述**: 发送短信验证码
- **参数**: 
```json
{
  "phone": "手机号",
  "type": "register|login|reset_password"
}
```
- **返回**: Boolean

#### 验证验证码
- **URL**: `POST /api/user/verify-sms`
- **描述**: 验证短信验证码
- **参数**: 
```json
{
  "phone": "手机号",
  "code": "验证码"
}
```
- **返回**: Boolean

### 11. 缓存管理

#### 清除用户缓存
- **URL**: `DELETE /api/user/cache`
- **描述**: 清除用户相关缓存
- **返回**: Boolean

---

## 行程相关接口 (TripController)

### 1. 行程基础功能

#### 获取行程列表
- **URL**: `GET /api/trips/list`
- **描述**: 获取行程列表（支持筛选）
- **参数**: Map<String, Object> (查询参数)
- **返回**: List<Trip>

#### 获取今日行程
- **URL**: `GET /api/trips/today`
- **描述**: 获取今天未出发的行程
- **参数**: Map<String, Object> (查询参数)
- **返回**: List<Trip>

#### 创建行程
- **URL**: `POST /api/trips/create`
- **描述**: 创建新行程
- **参数**: Trip对象
- **返回**: 行程ID (String)

#### 获取行程详情
- **URL**: `GET /api/trips/{tripId}`
- **描述**: 获取指定行程的详细信息
- **返回**: Trip对象

#### 更新行程
- **URL**: `PUT /api/trips/{tripId}`
- **描述**: 更新行程信息
- **参数**: Trip对象
- **返回**: Boolean

#### 删除行程
- **URL**: `DELETE /api/trips/{tripId}`
- **描述**: 删除指定行程
- **返回**: Boolean

### 2. 行程发布

#### 发布司机行程
- **URL**: `POST /api/trips/publish/driver`
- **描述**: 司机发布拼车行程
- **参数**: 
```json
{
  "driverId": "司机ID",
  "driverName": "司机姓名",
  "driverAvatar": "头像URL",
  "startLocation": "出发地",
  "endLocation": "目的地",
  "departureTime": "出发时间",
  "availableSeats": 3,
  "price": 38.00,
  "vehicleInfo": "车辆信息",
  "note": "备注"
}
```
- **返回**: 行程ID (String)

#### 发布乘客行程
- **URL**: `POST /api/trips/publish/passenger`
- **描述**: 乘客发布搭车需求
- **参数**: 
```json
{
  "passengerId": "乘客ID",
  "passengerName": "乘客姓名",
  "passengerAvatar": "头像URL",
  "startLocation": "出发地",
  "endLocation": "目的地",
  "departureTime": "出发时间",
  "passengerCount": 2,
  "pricePerPerson": 20.00,
  "note": "备注"
}
```
- **返回**: 行程ID (String)

### 3. 行程搜索

#### 搜索附近行程
- **URL**: `POST /api/trips/search/nearby`
- **描述**: 根据位置搜索附近的行程
- **参数**: 搜索条件对象
- **返回**: List<Trip>

### 4. 行程参与

#### 申请加入行程
- **URL**: `POST /api/trips/{tripId}/join`
- **描述**: 申请加入指定行程
- **参数**: 申请数据对象
- **返回**: Boolean

#### 获取行程参与者
- **URL**: `GET /api/trips/{tripId}/participants`
- **描述**: 获取行程的参与者列表
- **返回**: List<Map<String, Object>>

### 5. 行程状态管理

#### 取消行程
- **URL**: `PUT /api/trips/{tripId}/cancel`
- **描述**: 取消指定行程
- **参数**: 
```json
{
  "reason": "取消原因"
}
```
- **返回**: Boolean

#### 完成行程
- **URL**: `PUT /api/trips/{tripId}/complete`
- **描述**: 标记行程为已完成
- **返回**: Boolean

### 6. 用户行程

#### 获取用户行程
- **URL**: `GET /api/trips/user/{userId}?page=1&limit=10`
- **描述**: 获取指定用户的行程列表
- **参数**: 
  - page: 页码 (默认1)
  - limit: 每页条数 (默认10)
- **返回**: List<Trip>

---

## 数据模型

### User 用户实体
```java
{
  "id": "用户ID",
  "name": "用户姓名",
  "phone": "手机号",
  "avatar": "头像URL",
  "community": "居住小区",
  "vehicleBrand": "车辆品牌",
  "vehicleColor": "车辆颜色",
  "plateNumber": "车牌号",
  "balance": "余额",
  "coupons": "优惠券数量",
  "historyOrders": "历史订单数",
  "verificationStatus": "认证状态",
  "realName": "真实姓名",
  "idCard": "身份证号",
  "driverLicensePhoto": "驾驶证照片",
  "vehicleLicensePhoto": "行驶证照片",
  "createTime": "创建时间",
  "updateTime": "更新时间"
}
```

### Trip 行程实体
```java
{
  "id": "行程ID",
  "driverId": "司机ID",
  "driverName": "司机姓名",
  "driverAvatar": "司机头像",
  "startLocation": "出发地",
  "endLocation": "目的地",
  "departureTime": "出发时间",
  "availableSeats": "可用座位数",
  "passengerCount": "乘客数量",
  "price": "价格",
  "vehicleInfo": "车辆信息",
  "note": "备注",
  "type": "行程类型(car_seeking_people/people_seeking_car)",
  "status": "状态(available/full/cancelled/completed)",
  "createTime": "创建时间",
  "updateTime": "更新时间"
}
```

## 错误处理

API返回的错误格式：
```json
{
  "code": 500,
  "message": "错误信息描述",
  "data": null
}
```

常见错误码：
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

## 认证说明

当前版本为演示版本，使用固定的用户ID `user_001` 进行测试。
在生产环境中，应该实现完整的JWT token认证机制。

## 部署信息

- Java版本: 8+
- Spring Boot版本: 2.x
- 端口: 8080 (可配置)
- 数据存储: 当前使用内存存储（演示用途），生产环境建议使用MySQL/PostgreSQL

## 联系方式

如有API使用问题，请联系开发团队。