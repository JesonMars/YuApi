-- Manual Migration: Create vehicles table and migrate data
-- Run this script manually in your MySQL database

USE yugo_db;

-- 1. Create vehicles table (if not exists)
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

-- 2. Add vehicle_id column to trip_details table (if not exists)
-- Check if column exists before adding
SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'yugo_db'
    AND TABLE_NAME = 'trip_details'
    AND COLUMN_NAME = 'vehicle_id'
);

-- Add column only if it doesn't exist
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE trip_details ADD COLUMN vehicle_id VARCHAR(50) COMMENT ''车辆ID'' AFTER trip_id, ADD INDEX idx_vehicle_id (vehicle_id)',
    'SELECT ''Column vehicle_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Migrate existing users table vehicle data to vehicles table
-- Only migrate users who have vehicle information and don't already have a vehicle record
INSERT IGNORE INTO vehicles (id, user_id, plate_number, brand, color, is_default, create_time, update_time)
SELECT
    CONCAT('VEH-', SUBSTRING(MD5(CONCAT(u.id, NOW())), 1, 12)) as id,
    u.id as user_id,
    u.plate_number,
    u.vehicle_brand as brand,
    u.vehicle_color as color,
    TRUE as is_default,
    NOW() as create_time,
    NOW() as update_time
FROM users u
WHERE (u.vehicle_brand IS NOT NULL AND u.vehicle_brand != '')
   OR (u.vehicle_color IS NOT NULL AND u.vehicle_color != '')
   OR (u.plate_number IS NOT NULL AND u.plate_number != '')
   AND NOT EXISTS (
       SELECT 1 FROM vehicles v WHERE v.user_id = u.id
   );

-- 4. Show migration results
SELECT
    'Vehicles table created' AS status,
    (SELECT COUNT(*) FROM vehicles) AS total_vehicles,
    (SELECT COUNT(DISTINCT user_id) FROM vehicles) AS users_with_vehicles;

-- 5. Show sample of migrated data
SELECT
    v.id,
    v.user_id,
    v.plate_number,
    v.brand,
    v.color,
    v.is_default,
    u.name as user_name
FROM vehicles v
LEFT JOIN users u ON v.user_id = u.id
LIMIT 10;
