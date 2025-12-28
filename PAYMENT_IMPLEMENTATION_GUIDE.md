# 预定支付功能实现指南

## 数据库变更

### 1. 表结构重构
将原来的 `trip_participants` 表重命名为 `orders` 表，并扩展支付功能。

**迁移文件**: `V5__rename_trip_participants_to_orders.sql`

#### orders 表结构
```sql
- id: 订单ID
- trip_id: 关联的行程ID
- user_id: 乘客ID
- status: 订单状态 (pending, paid, confirmed, trip_started, trip_ended, cancelled, rejected)
- price: 订单金额（元）
- payment_method: 支付方式 (wechat, alipay)
- payment_status: 支付状态 (unpaid, paid, refunded)
- transaction_id: 微信支付交易号
- out_trade_no: 商户订单号
- pay_time: 支付时间
- pickup_point: 上车点
- dropoff_point: 下车点
- passenger_count: 乘车人数
- rating: 评分
- feedback: 评价内容
- order_time: 下单时间
- create_time: 创建时间
- update_time: 更新时间
```

### 2. 新增分账表

#### profit_sharing_receivers 表
用于存储司机的分账接收方信息。
```sql
- id: 分账接收方ID
- user_id: 用户ID（司机ID）
- type: 分账接收方类型 (MERCHANT_ID, PERSONAL_OPENID)
- account: 分账接收方账户
- name: 分账接收方姓名
- relation_type: 关系类型
- status: 状态 (active, inactive, deleted)
```

#### profit_sharing_records 表
用于记录每次分账操作。
```sql
- id: 分账记录ID
- order_id: 订单ID
- trip_id: 行程ID
- out_order_no: 商户分账单号
- transaction_id: 微信支付订单号
- profit_sharing_order_id: 微信分账单号
- status: 分账状态 (pending, processing, finished, closed)
- total_amount: 订单总金额
- driver_amount: 司机分账金额
- platform_amount: 平台分账金额
- promoter_amount: 推广员分账金额
- driver_receiver_id: 司机分账接收方ID
```

## 如何运行数据库迁移

### 方式一：使用 Flyway（推荐）
如果项目已配置 Flyway，启动应用时会自动执行迁移。

```bash
cd /Users/jackzhang/dev/local/ycheng/server/YuApi
mvn spring-boot:run
```

### 方式二：手动执行SQL
```bash
mysql -u your_username -p yugo_db < src/main/resources/db/migration/V5__rename_trip_participants_to_orders.sql
```

### 方式三：使用MySQL客户端
```sql
USE yugo_db;
SOURCE /Users/jackzhang/dev/local/ycheng/server/YuApi/src/main/resources/db/migration/V5__rename_trip_participants_to_orders.sql;
```

## 代码变更

### 1. OrderMapper.xml
- 更新了所有SQL查询，使用 JOIN trips 和 users 表来获取司机和行程信息
- 调整了字段映射以匹配新的 orders 表结构
- 所有查询现在会自动返回完整的订单信息，包括：
  - 订单基本信息
  - 司机信息（从 trips 和 users 表JOIN获取）
  - 行程路线信息
  - 支付状态

### 2. OrderServiceImpl
- 已适配新的表结构
- 所有数据库操作通过 OrderMapper 进行
- 不再使用内存存储（HashMap）

### 3. 前端API调用
前端API接口保持不变：
- `GET /orders/my` - 获取我的订单列表
- `GET /orders/{orderId}` - 获取订单详情
- `POST /orders/create` - 创建订单并支付
- `POST /orders/cancel` - 取消订单
- `POST /orders/rate` - 评价订单

## 预定支付流程

### 完整流程：

1. **用户选择行程并填写信息**
   - 选择上车点、下车点
   - 选择乘车人数
   - 计算总价

2. **创建订单**
   ```typescript
   // 前端调用
   const orderData = {
     tripId: 'trip_xxx',
     pickupPoint: '国贸地铁站',
     dropoffPoint: '廊坊师范学院',
     passengerCount: 1,
     totalAmount: 38.00
   };
   const response = await createOrder(orderData);
   ```

3. **后端处理**
   ```java
   // OrderServiceImpl.createOrderWithPayment()
   - 创建订单记录（status=pending, payment_status=unpaid）
   - 生成商户订单号 (out_trade_no)
   - 调用微信支付API获取支付参数
   - 返回订单ID和支付参数
   ```

4. **调起微信支付**
   ```typescript
   // 前端使用返回的支付参数调起微信支付
   uni.requestPayment({
     provider: 'wxpay',
     ...paymentParams,
     success: () => {
       // 跳转到订单详情页
       uni.redirectTo({
         url: `/pages/order/detail?orderId=${orderId}`
       });
     }
   });
   ```

5. **支付回调处理**
   ```java
   // OrderServiceImpl.handleWechatPayNotify()
   - 验证微信支付签名
   - 更新订单支付状态 (payment_status=paid, status=confirmed)
   - 记录交易号 (transaction_id) 和支付时间 (pay_time)
   - [可选] 执行分账操作
   ```

6. **查看订单详情**
   ```typescript
   // 用户在订单详情页查看订单状态
   const order = await getOrderById(orderId);
   ```

## 分账功能实现（可选）

### 1. 添加分账接收方
司机首次使用时需要添加分账接收方信息：

```java
// ProfitSharingServiceImpl.addReceiver()
profitSharingService.addReceiver(
    driverId,
    "PERSONAL_OPENID",  // 或 MERCHANT_ID
    openid,             // 或 商户号
    driverName,
    "SERVICE_PROVIDER"
);
```

### 2. 执行分账
订单完成后执行分账：

```java
// 行程结束时调用
profitSharingService.executeProfitSharing(
    orderId,
    totalAmount,
    platformRate  // 平台费率，例如 0.06 表示6%
);
```

### 3. 完结分账
不再对此订单进行分账操作：

```java
profitSharingService.finishProfitSharing(
    orderId,
    "订单已完成，完结分账"
);
```

## 注意事项

1. **微信支付配置**
   - 需要在 `WechatPayUtils.java` 中配置真实的微信支付参数
   - APP_ID, MCH_ID, API_KEY 等

2. **支付回调**
   - 确保服务器可以接收微信支付回调
   - 回调URL需要配置在微信商户平台

3. **分账功能**
   - 需要开通微信支付分账功能
   - 司机需要完成身份认证
   - 平台需要是服务商模式

4. **数据迁移**
   - 如果生产环境有数据，迁移前请备份
   - trip_participants 表会被重命名，确认没有其他地方引用这个表名

## 测试流程

1. 运行数据库迁移
2. 启动后端服务
3. 测试创建订单API
4. 测试查询订单API
5. 测试取消订单API
6. 测试评价订单API
7. [可选] 测试分账功能

## 常见问题

**Q: 为什么要重命名 trip_participants 为 orders？**
A: trip_participants 表在添加支付功能后，实质上就是订单表。重命名使代码语义更清晰，更符合业务逻辑。

**Q: 原有的 trip_participants 数据会丢失吗？**
A: 不会。RENAME TABLE 操作只是改名，数据完全保留。

**Q: 如何回滚迁移？**
A: 如需回滚，执行：
```sql
RENAME TABLE orders TO trip_participants;
-- 然后删除新增的字段和表
```

**Q: 订单表中为什么没有 driver_id？**
A: driver_id 存储在 trips 表中。orders 通过 trip_id 关联到 trips，再获取司机信息。这样设计避免了数据冗余。
