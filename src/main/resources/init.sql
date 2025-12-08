-- 创建数据库
CREATE DATABASE IF NOT EXISTS yugo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE yugo_db;

-- 应用配置表
CREATE TABLE IF NOT EXISTS app_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) DEFAULT 'app' COMMENT '配置类型',
    description VARCHAR(255) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '应用配置表';

-- 地点表
CREATE TABLE IF NOT EXISTS location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '地点名称',
    address VARCHAR(255) COMMENT '详细地址',
    longitude DECIMAL(10, 7) COMMENT '经度',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    type VARCHAR(50) DEFAULT 'hot' COMMENT '地点类型：hot-热门地点',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '地点表';

-- 插入基础配置数据
INSERT INTO app_config (config_key, config_value, config_type, description) VALUES
-- 应用基本信息
('app_name', 'PinGo', 'app', '应用名称'),
('app_slogan', '我们的使命，让生活变得更美好！', 'app', '应用标语'),
('welcome_image', '/static/logo.png', 'app', '欢迎页图片'),
('primary_color', '#4A90E2', 'app', '主色调'),
('secondary_color', '#7ED321', 'app', '辅助色调'),

-- 首页按钮
('home_button_driver', '顺风车', 'ui', '首页司机按钮文字'),
('home_button_passenger', '通勤', 'ui', '首页乘客按钮文字'),

-- 底部导航栏
('tab_bar_square', '广场', 'ui', '底部导航-广场'),
('tab_bar_trip', '行程', 'ui', '底部导航-行程'),
('tab_bar_message', '消息', 'ui', '底部导航-消息'),
('tab_bar_profile', '我的', 'ui', '底部导航-我的'),

-- 页面文字
('page_text_welcome_mode_tip', '如果想更改模式，请在个人中心重新选择', 'text', '欢迎页模式提示'),
('page_text_commute_route_title', '完善通勤线路', 'text', '通勤路线页面标题'),
('page_text_commute_route_desc', '我们将用于推荐与你一同通勤上下班的邻居，且未经你同意不会用于其他用途。', 'text', '通勤路线页面描述'),
('page_text_publish_trip_title', '发布行程', 'text', '发布行程页面标题'),
('page_text_waypoint_title', '途经点', 'text', '途经点标题'),
('page_text_datetime_title', '时间及其它', 'text', '时间设置标题'),
('page_text_estimated_income', '预计收入', 'text', '预计收入标签'),
('page_text_publish_button', '发布', 'text', '发布按钮文字'),
('page_text_waiting_passenger', '暂无乘客加入', 'text', '等待乘客提示'),
('page_text_passenger_joined', '已有乘客加入', 'text', '乘客加入提示'),
('page_text_trip_rules', '限号让行；车内禁止吸烟；轻关车门；乘客...', 'text', '行程规则'),

-- 功能模块
('page_text_my_drive', '我要开车', 'text', '我要开车标题'),
('page_text_my_ride', '我要用车', 'text', '我要用车标题'),
('page_text_find_driver', '车找人', 'text', '车找人'),
('page_text_find_passenger', '人找车', 'text', '人找车'),
('page_text_find_cargo', '车找物', 'text', '车找物'),
('page_text_find_vehicle', '物找车', 'text', '物找车'),

-- 用户相关
('page_text_personal_info', '个人资料', 'text', '个人资料'),
('page_text_driver_verify', '车主认证', 'text', '车主认证'),
('page_text_subscribe_msg', '订阅消息', 'text', '订阅消息'),
('page_text_app_mode', '应用模式', 'text', '应用模式'),
('page_text_invite_friend', '邀请好友', 'text', '邀请好友'),
('page_text_manage_center', '管理中心', 'text', '管理中心'),

-- 通知消息
('notification_passenger_wait', '已通知附近的邻居，等待响应中。', 'notification', '等待乘客通知'),
('notification_trip_change', '道路沿线、地铁站可能有多个上下车点，请主动跟乘客沟通。若有行程变动请提前通知。', 'notification', '行程变动通知');

-- 插入热门地点数据
INSERT INTO location (name, address, longitude, latitude, type, sort_order, status) VALUES
('锐创', '锐创国际中心', 116.4074, 39.9042, 'hot', 1, 1),
('望京SOHO', '望京SOHO', 116.4733, 39.9956, 'hot', 2, 1),
('首开', '首开广场', 116.3974, 39.9042, 'hot', 3, 1),
('保利', '保利国际广场', 116.4074, 39.9142, 'hot', 4, 1),
('金辉', '金汇大厦', 116.4174, 39.9242, 'hot', 5, 1),
('启明', '启明国际大厦', 116.4274, 39.9342, 'hot', 6, 1);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar VARCHAR(500),
    community VARCHAR(200),
    vehicle_brand VARCHAR(50),
    vehicle_color VARCHAR(30),
    plate_number VARCHAR(20),
    balance DECIMAL(10,2) DEFAULT 0.00,
    coupons INT DEFAULT 0,
    history_orders INT DEFAULT 0,
    verification_status ENUM('none', 'pending', 'verified', 'rejected') DEFAULT 'none',
    real_name VARCHAR(50),
    id_card VARCHAR(30),
    driver_license_photo VARCHAR(500),
    vehicle_license_photo VARCHAR(500),
    wechat_openid VARCHAR(100),
    wechat_unionid VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_wechat_openid (wechat_openid),
    INDEX idx_wechat_unionid (wechat_unionid)
) COMMENT '用户表';

-- 微信用户表
CREATE TABLE IF NOT EXISTS wechat_users (
    id VARCHAR(50) PRIMARY KEY,
    openid VARCHAR(100) UNIQUE NOT NULL,
    unionid VARCHAR(100),
    session_key VARCHAR(100),
    nick_name VARCHAR(100),
    avatar_url VARCHAR(500),
    gender TINYINT,
    city VARCHAR(50),
    province VARCHAR(50),
    country VARCHAR(50),
    language VARCHAR(10),
    phone_number VARCHAR(20),
    pure_phone_number VARCHAR(20),
    country_code VARCHAR(10),
    internal_user_id VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP,
    INDEX idx_openid (openid),
    INDEX idx_unionid (unionid),
    INDEX idx_internal_user_id (internal_user_id),
    FOREIGN KEY (internal_user_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT '微信用户表';

-- 行程表
CREATE TABLE IF NOT EXISTS trips (
    id VARCHAR(50) PRIMARY KEY,
    driver_id VARCHAR(50) NOT NULL,
    driver_name VARCHAR(100),
    driver_avatar VARCHAR(500),
    start_location VARCHAR(200) NOT NULL,
    end_location VARCHAR(200) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    available_seats INT,
    passenger_count INT,
    price DECIMAL(8,2),
    vehicle_info VARCHAR(200),
    note TEXT,
    type ENUM('car_seeking_people', 'people_seeking_car') NOT NULL,
    status ENUM('available', 'full', 'cancelled', 'completed') DEFAULT 'available',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_driver_id (driver_id),
    INDEX idx_departure_time (departure_time),
    INDEX idx_status (status),
    INDEX idx_type (type)
) COMMENT '行程表';

-- 用户关注表
CREATE TABLE IF NOT EXISTS user_follows (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    target_user_id VARCHAR(50) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_follow (user_id, target_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target_user_id (target_user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户关注表';

-- 黑名单表
CREATE TABLE IF NOT EXISTS user_blacklist (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    target_user_id VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_blacklist (user_id, target_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target_user_id (target_user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '黑名单表';

-- 用户位置表
CREATE TABLE IF NOT EXISTS user_locations (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(300) NOT NULL,
    longitude DECIMAL(10,7),
    latitude DECIMAL(10,7),
    type ENUM('home', 'company', 'other') DEFAULT 'other',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_location (longitude, latitude),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户位置表';

-- 钱包交易记录表
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    type ENUM('recharge', 'withdraw', 'consume', 'refund', 'reward') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    payment_method VARCHAR(50),
    status ENUM('pending', 'success', 'failed') DEFAULT 'success',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '钱包交易记录表';

-- 用户设置表
CREATE TABLE IF NOT EXISTS user_settings (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    message_notification BOOLEAN DEFAULT TRUE,
    trip_reminder BOOLEAN DEFAULT TRUE,
    sound_enabled BOOLEAN DEFAULT TRUE,
    vibration_enabled BOOLEAN DEFAULT FALSE,
    night_mode BOOLEAN DEFAULT FALSE,
    language VARCHAR(10) DEFAULT 'zh_CN',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户设置表';

-- 行程参与者表
CREATE TABLE IF NOT EXISTS trip_participants (
    id VARCHAR(50) PRIMARY KEY,
    trip_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    status ENUM('pending', 'confirmed', 'rejected', 'cancelled') DEFAULT 'pending',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_participant (trip_id, user_id),
    INDEX idx_trip_id (trip_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '行程参与者表';