-- 将 trip_participants 重命名为 orders，并扩展支付功能

-- 1. 重命名表
RENAME TABLE trip_participants TO orders;

-- 2. 添加支付相关字段
ALTER TABLE orders
ADD COLUMN price DECIMAL(10, 2) COMMENT '订单金额（元）' AFTER status,
ADD COLUMN payment_method VARCHAR(50) DEFAULT 'wechat' COMMENT '支付方式：wechat-微信，alipay-支付宝' AFTER price,
ADD COLUMN payment_status ENUM('unpaid', 'paid', 'refunded') DEFAULT 'unpaid' COMMENT '支付状态' AFTER payment_method,
ADD COLUMN transaction_id VARCHAR(100) COMMENT '微信支付交易号' AFTER payment_status,
ADD COLUMN out_trade_no VARCHAR(100) UNIQUE COMMENT '商户订单号' AFTER transaction_id,
ADD COLUMN pay_time TIMESTAMP NULL COMMENT '支付时间' AFTER out_trade_no;

-- 3. 添加行程详情字段
ALTER TABLE orders
ADD COLUMN pickup_point VARCHAR(200) COMMENT '上车点' AFTER pay_time,
ADD COLUMN dropoff_point VARCHAR(200) COMMENT '下车点' AFTER pickup_point,
ADD COLUMN passenger_count INT DEFAULT 1 COMMENT '乘车人数' AFTER dropoff_point;

-- 4. 添加评价字段
ALTER TABLE orders
ADD COLUMN rating INT COMMENT '评分（1-5）' AFTER passenger_count,
ADD COLUMN feedback TEXT COMMENT '评价内容' AFTER rating;

-- 5. 添加更新时间字段
ALTER TABLE orders
ADD COLUMN create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER feedback,
ADD COLUMN update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

-- 6. 重命名原有的 join_time 为 order_time（保持向后兼容）
ALTER TABLE orders
CHANGE COLUMN join_time order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间';

-- 7. 扩展状态枚举
ALTER TABLE orders
MODIFY COLUMN status ENUM(
    'pending',
    'paid',
    'confirmed',
    'trip_started',
    'trip_ended',
    'cancelled',
    'rejected'
) DEFAULT 'pending' COMMENT '订单状态：pending-待支付，paid-已支付，confirmed-已确认，trip_started-行程开始，trip_ended-行程结束，cancelled-已取消，rejected-已拒绝';

-- 8. 添加订单相关索引
CREATE INDEX idx_payment_status ON orders(payment_status);
CREATE INDEX idx_out_trade_no ON orders(out_trade_no);
CREATE INDEX idx_order_time ON orders(order_time);
CREATE INDEX idx_create_time ON orders(create_time);

-- 9. 创建分账接收方表
CREATE TABLE IF NOT EXISTS profit_sharing_receivers (
    id VARCHAR(50) PRIMARY KEY COMMENT '分账接收方ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID（司机ID）',
    type VARCHAR(50) NOT NULL COMMENT '分账接收方类型：MERCHANT_ID-商户号，PERSONAL_OPENID-个人openid',
    account VARCHAR(100) NOT NULL COMMENT '分账接收方账户',
    name VARCHAR(100) COMMENT '分账接收方姓名',
    relation_type VARCHAR(50) COMMENT '与分账方的关系类型：SERVICE_PROVIDER-服务商，STORE-门店，STAFF-员工',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-激活，inactive-未激活，deleted-已删除',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    UNIQUE KEY unique_user_receiver (user_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '分账接收方表';

-- 10. 创建分账记录表
CREATE TABLE IF NOT EXISTS profit_sharing_records (
    id VARCHAR(50) PRIMARY KEY COMMENT '分账记录ID',
    order_id VARCHAR(50) NOT NULL COMMENT '订单ID（对应orders表的ID）',
    trip_id VARCHAR(50) NOT NULL COMMENT '行程ID',
    out_order_no VARCHAR(100) NOT NULL COMMENT '商户分账单号',
    transaction_id VARCHAR(100) COMMENT '微信支付订单号',
    profit_sharing_order_id VARCHAR(100) COMMENT '微信分账单号',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '分账状态：pending-待分账，processing-处理中，finished-分账完成，closed-已关闭',

    -- 分账金额（单位：元）
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    driver_amount DECIMAL(10, 2) NOT NULL COMMENT '司机分账金额',
    platform_amount DECIMAL(10, 2) NOT NULL COMMENT '平台分账金额',
    promoter_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '推广员分账金额',

    -- 分账接收方ID
    driver_receiver_id VARCHAR(50) COMMENT '司机分账接收方ID',
    promoter_receiver_id VARCHAR(50) COMMENT '推广员分账接收方ID',

    -- 时间
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    finish_time TIMESTAMP NULL COMMENT '完成时间',

    -- 错误信息
    error_description TEXT COMMENT '错误描述',

    INDEX idx_order_id (order_id),
    INDEX idx_trip_id (trip_id),
    INDEX idx_out_order_no (out_order_no),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),

    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_receiver_id) REFERENCES profit_sharing_receivers(id) ON DELETE SET NULL
) COMMENT '分账记录表';
