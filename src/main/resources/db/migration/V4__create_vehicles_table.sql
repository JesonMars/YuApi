-- V4: 创建车辆表
-- 将车辆信息从users表分离，支持一个用户拥有多辆车

CREATE TABLE IF NOT EXISTS vehicles (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL COMMENT '车主用户ID',

    -- 车辆基本信息
    vin VARCHAR(17) COMMENT '车架号（Vehicle Identification Number）',
    plate_number VARCHAR(20) COMMENT '车牌号',
    brand VARCHAR(50) COMMENT '车辆品牌',
    color VARCHAR(20) COMMENT '车辆颜色',
    model VARCHAR(50) COMMENT '车型',

    -- 默认车辆标识
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为用户的默认车辆',

    -- 时间戳
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_plate_number (plate_number),
    INDEX idx_vin (vin),

    -- 约束
    CONSTRAINT unique_vin UNIQUE (vin),
    CONSTRAINT unique_plate_number UNIQUE (plate_number)
) COMMENT '车辆表';

-- 在trip_details表中添加vehicle_id字段
ALTER TABLE trip_details
ADD COLUMN vehicle_id VARCHAR(50) COMMENT '车辆ID' AFTER trip_id,
ADD INDEX idx_vehicle_id (vehicle_id);

-- 迁移现有users表中的车辆数据到vehicles表
-- 只迁移有车辆信息的用户
INSERT INTO vehicles (id, user_id, plate_number, brand, color, is_default, create_time, update_time)
SELECT
    CONCAT('VEH-', UUID()) as id,
    id as user_id,
    plate_number,
    vehicle_brand as brand,
    vehicle_color as color,
    TRUE as is_default,
    NOW() as create_time,
    NOW() as update_time
FROM users
WHERE (vehicle_brand IS NOT NULL AND vehicle_brand != '')
   OR (vehicle_color IS NOT NULL AND vehicle_color != '')
   OR (plate_number IS NOT NULL AND plate_number != '');

-- 注意：保留users表中的vehicle_brand, vehicle_color, plate_number字段以保持向后兼容性
-- 后续可以在确认迁移成功后手动删除这些字段
