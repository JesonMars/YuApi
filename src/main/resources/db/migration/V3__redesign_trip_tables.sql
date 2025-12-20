-- V3: 重新设计行程表结构
-- 1. 创建用户通勤配置表
CREATE TABLE IF NOT EXISTS user_commute_config (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    timing VARCHAR(20) NOT NULL COMMENT '时段：tonight/tomorrow',
    pickup_points TEXT COMMENT '上车点列表（JSON数组）',
    dropoff_points TEXT COMMENT '下车点列表（JSON数组）',
    -- 司机配置
    default_seat_count INT DEFAULT 3 COMMENT '默认座位数',
    default_price_per_seat BIGINT DEFAULT 2000 COMMENT '默认单价/位（分，2000=20元）',
    default_recurring_type VARCHAR(20) DEFAULT '仅当次' COMMENT '默认循环类型',
    default_notes TEXT COMMENT '默认备注',
    -- 乘客配置
    default_passenger_count INT DEFAULT 1 COMMENT '默认乘车人数',
    default_offer_price BIGINT COMMENT '默认出价（分）',
    -- 时间戳
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_timing (user_id, timing),
    INDEX idx_user_id (user_id)
) COMMENT '用户通勤配置表';

-- 2. 创建行程详情扩展表
CREATE TABLE IF NOT EXISTS trip_details (
    id VARCHAR(50) PRIMARY KEY,
    trip_id VARCHAR(50) NOT NULL UNIQUE,
    -- 途经点
    pickup_points TEXT COMMENT '上车点列表（JSON数组）',
    dropoff_points TEXT COMMENT '下车点列表（JSON数组）',
    -- 司机信息
    driver_name VARCHAR(100) COMMENT '司机姓名',
    driver_avatar VARCHAR(500) COMMENT '司机头像',
    driver_phone VARCHAR(20) COMMENT '司机电话',
    vehicle_info VARCHAR(200) COMMENT '车辆信息',
    plate_number VARCHAR(20) COMMENT '车牌号',
    -- 行程备注
    notes TEXT COMMENT '行程备注',
    -- 价格明细（单位：分）
    price_per_seat BIGINT COMMENT '单价/位（分）',
    seat_count INT COMMENT '座位数',
    total_income BIGINT COMMENT '预计总收入（分）',
    base_price BIGINT COMMENT '基础价格（分）',
    extra_fee BIGINT COMMENT '附加费用（分）',
    -- 乘客信息
    passenger_count INT COMMENT '乘车人数',
    passenger_names TEXT COMMENT '乘客姓名列表',
    -- 时间戳
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_trip_id (trip_id)
) COMMENT '行程详情扩展表';

-- 3. 重构trips表（删除旧表，创建新表）
DROP TABLE IF EXISTS trip_details_temp;

DROP TABLE IF EXISTS trips_backup;

-- 备份旧数据（如果需要）
CREATE TABLE IF NOT EXISTS trips_backup AS SELECT * FROM trips;

-- 删除旧表
DROP TABLE IF EXISTS trips;

-- 创建新的精简trips表
CREATE TABLE trips (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL COMMENT '发布者ID（司机或乘客）',
    type ENUM(
        'car_seeking_people',
        'people_seeking_car'
    ) NOT NULL COMMENT '类型：车找人/人找车',
    -- 核心地点信息
    start_city VARCHAR(50) COMMENT '起点城市',
    start_location VARCHAR(200) NOT NULL COMMENT '起点',
    start_longitude DECIMAL(10, 7) COMMENT '起点经度',
    start_latitude DECIMAL(10, 7) COMMENT '起点纬度',
    end_city VARCHAR(50) COMMENT '终点城市',
    end_location VARCHAR(200) NOT NULL COMMENT '终点',
    end_longitude DECIMAL(10, 7) COMMENT '终点经度',
    end_latitude DECIMAL(10, 7) COMMENT '终点纬度',
    -- 核心时间信息
    departure_time TIMESTAMP NOT NULL COMMENT '出发时间',
    timing VARCHAR(20) COMMENT '时段：tonight/tomorrow',
    -- 核心数量信息
    available_seats INT COMMENT '可用座位数（车找人）',
    booked_seats INT DEFAULT 0 COMMENT '已预订座位数',
    -- 核心价格信息（单位：分）
    price BIGINT COMMENT '价格（车找人是单价，人找车是总价，单位：分）',
    -- 状态
    status ENUM(
        'available',
        'full',
        'cancelled',
        'completed',
        'expired'
    ) DEFAULT 'available',
    -- 时间戳
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_departure_time (departure_time),
    INDEX idx_status (status),
    INDEX idx_timing (timing),
    INDEX idx_start_city (start_city),
    INDEX idx_end_city (end_city),
    INDEX idx_start_location (
        start_longitude,
        start_latitude
    ),
    INDEX idx_end_location (end_longitude, end_latitude)
) COMMENT '行程表（精简版）';

-- 4. 删除旧的TripMapper.xml备份
-- （需要手动更新Mapper文件）