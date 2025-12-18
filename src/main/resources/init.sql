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
INSERT INTO
    app_config (
        config_key,
        config_value,
        config_type,
        description
    )
VALUES
    -- 应用基本信息
    (
        'app_name',
        'PinGo',
        'app',
        '应用名称'
    ),
    (
        'app_slogan',
        '我们的使命，让生活变得更美好！',
        'app',
        '应用标语'
    ),
    (
        'welcome_image',
        '/static/logo.png',
        'app',
        '欢迎页图片'
    ),
    (
        'primary_color',
        '#4A90E2',
        'app',
        '主色调'
    ),
    (
        'secondary_color',
        '#7ED321',
        'app',
        '辅助色调'
    ),

-- 首页按钮
(
    'home_button_driver',
    '顺风车',
    'ui',
    '首页司机按钮文字'
),
(
    'home_button_passenger',
    '通勤',
    'ui',
    '首页乘客按钮文字'
),

-- 底部导航栏
(
    'tab_bar_square',
    '广场',
    'ui',
    '底部导航-广场'
),
(
    'tab_bar_trip',
    '行程',
    'ui',
    '底部导航-行程'
),
(
    'tab_bar_message',
    '消息',
    'ui',
    '底部导航-消息'
),
(
    'tab_bar_profile',
    '我的',
    'ui',
    '底部导航-我的'
),

-- 页面文字
(
    'page_text_welcome_mode_tip',
    '如果想更改模式，请在个人中心重新选择',
    'text',
    '欢迎页模式提示'
),
(
    'page_text_commute_route_title',
    '完善通勤线路',
    'text',
    '通勤路线页面标题'
),
(
    'page_text_commute_route_desc',
    '我们将用于推荐与你一同通勤上下班的邻居，且未经你同意不会用于其他用途。',
    'text',
    '通勤路线页面描述'
),
(
    'page_text_publish_trip_title',
    '发布行程',
    'text',
    '发布行程页面标题'
),
(
    'page_text_waypoint_title',
    '途经点',
    'text',
    '途经点标题'
),
(
    'page_text_datetime_title',
    '时间及其它',
    'text',
    '时间设置标题'
),
(
    'page_text_estimated_income',
    '预计收入',
    'text',
    '预计收入标签'
),
(
    'page_text_publish_button',
    '发布',
    'text',
    '发布按钮文字'
),
(
    'page_text_waiting_passenger',
    '暂无乘客加入',
    'text',
    '等待乘客提示'
),
(
    'page_text_passenger_joined',
    '已有乘客加入',
    'text',
    '乘客加入提示'
),
(
    'page_text_trip_rules',
    '限号让行；车内禁止吸烟；轻关车门；乘客...',
    'text',
    '行程规则'
),

-- 功能模块
(
    'page_text_my_drive',
    '我要开车',
    'text',
    '我要开车标题'
),
(
    'page_text_my_ride',
    '我要用车',
    'text',
    '我要用车标题'
),
(
    'page_text_find_driver',
    '车找人',
    'text',
    '车找人'
),
(
    'page_text_find_passenger',
    '人找车',
    'text',
    '人找车'
),
(
    'page_text_find_cargo',
    '车找物',
    'text',
    '车找物'
),
(
    'page_text_find_vehicle',
    '物找车',
    'text',
    '物找车'
),

-- 用户相关
(
    'page_text_personal_info',
    '个人资料',
    'text',
    '个人资料'
),
(
    'page_text_driver_verify',
    '车主认证',
    'text',
    '车主认证'
),
(
    'page_text_subscribe_msg',
    '订阅消息',
    'text',
    '订阅消息'
),
(
    'page_text_app_mode',
    '应用模式',
    'text',
    '应用模式'
),
(
    'page_text_invite_friend',
    '邀请好友',
    'text',
    '邀请好友'
),
(
    'page_text_manage_center',
    '管理中心',
    'text',
    '管理中心'
),

-- 通知消息
(
    'notification_passenger_wait',
    '已通知附近的邻居，等待响应中。',
    'notification',
    '等待乘客通知'
),
(
    'notification_trip_change',
    '道路沿线、地铁站可能有多个上下车点，请主动跟乘客沟通。若有行程变动请提前通知。',
    'notification',
    '行程变动通知'
);

-- 插入热门地点数据
INSERT INTO
    location (
        name,
        address,
        longitude,
        latitude,
        type,
        sort_order,
        status
    )
VALUES (
        '锐创',
        '锐创国际中心',
        116.4074,
        39.9042,
        'hot',
        1,
        1
    ),
    (
        '望京SOHO',
        '望京SOHO',
        116.4733,
        39.9956,
        'hot',
        2,
        1
    ),
    (
        '首开',
        '首开广场',
        116.3974,
        39.9042,
        'hot',
        3,
        1
    ),
    (
        '保利',
        '保利国际广场',
        116.4074,
        39.9142,
        'hot',
        4,
        1
    ),
    (
        '金辉',
        '金汇大厦',
        116.4174,
        39.9242,
        'hot',
        5,
        1
    ),
    (
        '启明',
        '启明国际大厦',
        116.4274,
        39.9342,
        'hot',
        6,
        1
    );

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
    balance DECIMAL(10, 2) DEFAULT 0.00,
    coupons INT DEFAULT 0,
    history_orders INT DEFAULT 0,
    verification_status ENUM(
        'none',
        'pending',
        'verified',
        'rejected'
    ) DEFAULT 'none',
    real_name VARCHAR(50),
    id_card VARCHAR(30),
    driver_license_photo VARCHAR(500),
    vehicle_license_photo VARCHAR(500),
    wechat_openid VARCHAR(100),
    wechat_unionid VARCHAR(100),
    first_setup_completed TINYINT DEFAULT 0 COMMENT '是否完成首次设置：0-未完成，1-已完成',
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
    FOREIGN KEY (internal_user_id) REFERENCES users (id) ON DELETE SET NULL
) COMMENT '微信用户表';

-- 行程表
CREATE TABLE IF NOT EXISTS trips (
    id VARCHAR(50) PRIMARY KEY,
    driver_id VARCHAR(50) NOT NULL,
    driver_name VARCHAR(100),
    driver_avatar VARCHAR(500),
    start_location VARCHAR(200) NOT NULL,
    end_location VARCHAR(200) NOT NULL,
    start_longitude DECIMAL(10, 7) COMMENT '起点经度',
    start_latitude DECIMAL(10, 7) COMMENT '起点纬度',
    end_longitude DECIMAL(10, 7) COMMENT '终点经度',
    end_latitude DECIMAL(10, 7) COMMENT '终点纬度',
    departure_time TIMESTAMP NOT NULL,
    available_seats INT,
    passenger_count INT,
    price DECIMAL(8, 2),
    vehicle_info VARCHAR(200),
    note TEXT,
    type ENUM(
        'car_seeking_people',
        'people_seeking_car'
    ) NOT NULL,
    status ENUM(
        'available',
        'full',
        'cancelled',
        'completed'
    ) DEFAULT 'available',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_driver_id (driver_id),
    INDEX idx_departure_time (departure_time),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_start_location (
        start_longitude,
        start_latitude
    ),
    INDEX idx_end_location (end_longitude, end_latitude)
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
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users (id) ON DELETE CASCADE
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
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '黑名单表';

-- 用户位置表
CREATE TABLE IF NOT EXISTS user_locations (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(50) COMMENT '所在城市',
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    type ENUM('home', 'company', 'other') DEFAULT 'other',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_location (longitude, latitude),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '用户位置表';

-- 钱包交易记录表
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    type ENUM(
        'recharge',
        'withdraw',
        'consume',
        'refund',
        'reward'
    ) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500),
    payment_method VARCHAR(50),
    status ENUM(
        'pending',
        'success',
        'failed'
    ) DEFAULT 'success',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
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
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '用户设置表';

-- 用户通勤配置表
CREATE TABLE IF NOT EXISTS user_commute_config (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    timing VARCHAR(20) NOT NULL COMMENT 'tonight or tomorrow',
    pickup_points TEXT COMMENT '上车点JSON',
    dropoff_points TEXT COMMENT '下车点JSON',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_timing (user_id, timing),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '用户通勤配置表';

-- 行程参与者表
CREATE TABLE IF NOT EXISTS trip_participants (
    id VARCHAR(50) PRIMARY KEY,
    trip_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    status ENUM(
        'pending',
        'confirmed',
        'rejected',
        'cancelled'
    ) DEFAULT 'pending',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_participant (trip_id, user_id),
    INDEX idx_trip_id (trip_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '行程参与者表';

-- 城市表
CREATE TABLE IF NOT EXISTS cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL UNIQUE COMMENT '城市名称',
    city_code VARCHAR(20) COMMENT '城市编码',
    province VARCHAR(100) COMMENT '省份',
    adcode VARCHAR(20) COMMENT '区域编码',
    center_lng DECIMAL(10, 7) COMMENT '中心点经度',
    center_lat DECIMAL(10, 7) COMMENT '中心点纬度',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city_name (city_name),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) COMMENT '城市表';

-- 地址搜索缓存表
CREATE TABLE IF NOT EXISTS address_search_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL COMMENT '城市名称',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    results TEXT COMMENT '搜索结果JSON',
    source VARCHAR(50) DEFAULT 'amap' COMMENT '数据源：amap-高德，baidu-百度',
    hit_count INT DEFAULT 1 COMMENT '命中次数',
    last_hit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后命中时间',
    expire_time TIMESTAMP COMMENT '过期时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_search (city_name, keyword, source),
    INDEX idx_city_keyword (city_name, keyword),
    INDEX idx_expire_time (expire_time),
    INDEX idx_hit_count (hit_count)
) COMMENT '地址搜索缓存表';

-- 插入城市数据
INSERT INTO
    cities (
        city_name,
        city_code,
        province,
        adcode,
        center_lng,
        center_lat,
        status,
        sort_order
    )
VALUES
    -- 直辖市
    (
        '北京市',
        'bj',
        '北京',
        '110000',
        116.405285,
        39.904989,
        1,
        1
    ),
    (
        '天津市',
        'tj',
        '天津',
        '120000',
        117.190182,
        39.125596,
        1,
        2
    ),
    (
        '上海市',
        'sh',
        '上海',
        '310000',
        121.472644,
        31.231706,
        1,
        3
    ),
    (
        '重庆市',
        'cq',
        '重庆',
        '500000',
        106.504962,
        29.533155,
        1,
        4
    ),
    -- 广东省
    (
        '广州市',
        'gz',
        '广东省',
        '440100',
        113.280637,
        23.125178,
        1,
        5
    ),
    (
        '深圳市',
        'sz',
        '广东省',
        '440300',
        114.085947,
        22.547,
        1,
        6
    ),
    (
        '杭州市',
        'hangzhou',
        '浙江省',
        '330100',
        120.153576,
        30.287459,
        1,
        7
    ),
    (
        '宁波市',
        'nb',
        '浙江省',
        '330200',
        121.549792,
        29.868388,
        1,
        8
    ),
    (
        '温州市',
        'wz',
        '浙江省',
        '330300',
        120.672111,
        28.000575,
        1,
        9
    ),
    (
        '东莞市',
        'dg',
        '广东省',
        '441900',
        113.746262,
        23.046237,
        1,
        10
    ),
    (
        '佛山市',
        'fs',
        '广东省',
        '440600',
        113.122717,
        23.028762,
        1,
        11
    ),
    (
        '惠州市',
        'hz',
        '广东省',
        '441300',
        114.412599,
        23.079404,
        1,
        12
    ),
    (
        '中山市',
        'zs',
        '广东省',
        '442000',
        113.382391,
        22.521113,
        1,
        13
    ),
    (
        '珠海市',
        'zh',
        '广东省',
        '440400',
        113.553986,
        22.224979,
        1,
        14
    ),
    (
        '南京市',
        'nj',
        '江苏省',
        '320100',
        118.767413,
        32.041544,
        1,
        15
    ),
    (
        '苏州市',
        'suzhou',
        '江苏省',
        '320500',
        120.619585,
        31.299379,
        1,
        16
    ),
    (
        '无锡市',
        'wx',
        '江苏省',
        '320200',
        120.301663,
        31.574729,
        1,
        17
    ),
    (
        '常州市',
        'cz',
        '江苏省',
        '320400',
        119.946973,
        31.772752,
        1,
        18
    ),
    (
        '济南市',
        'jinan',
        '山东省',
        '370100',
        117.000923,
        36.675807,
        1,
        19
    ),
    (
        '青岛市',
        'qingdao',
        '山东省',
        '370200',
        120.355173,
        36.082982,
        1,
        20
    ),
    -- 河北省
    (
        '石家庄市',
        'sjz',
        '河北省',
        '130100',
        114.502461,
        38.045474,
        1,
        21
    ),
    (
        '唐山市',
        'ts',
        '河北省',
        '130200',
        118.175393,
        39.635113,
        1,
        22
    ),
    (
        '保定市',
        'bd',
        '河北省',
        '130600',
        115.482331,
        38.867657,
        1,
        23
    ),
    (
        '廊坊市',
        'lf',
        '河北省',
        '131000',
        116.7,
        39.529,
        1,
        24
    ),
    -- 江苏省续
    (
        '镇江市',
        'zj',
        '江苏省',
        '321100',
        119.452753,
        32.204402,
        1,
        25
    ),
    (
        '南通市',
        'nt',
        '江苏省',
        '320600',
        120.864608,
        32.016212,
        1,
        26
    ),
    (
        '泰州市',
        'taizhou_js',
        '江苏省',
        '321200',
        119.915176,
        32.484882,
        1,
        27
    ),
    (
        '扬州市',
        'yz',
        '江苏省',
        '321000',
        119.421003,
        32.393159,
        1,
        28
    ),
    (
        '徐州市',
        'xuzhou',
        '江苏省',
        '320300',
        117.184811,
        34.261792,
        1,
        29
    ),
    -- 浙江省续
    (
        '嘉兴市',
        'jx',
        '浙江省',
        '330400',
        120.750865,
        30.762653,
        1,
        30
    ),
    (
        '湖州市',
        'huzhou',
        '浙江省',
        '330500',
        120.102398,
        30.867198,
        1,
        31
    ),
    (
        '绍兴市',
        'sx',
        '浙江省',
        '330600',
        120.582112,
        29.997117,
        1,
        32
    ),
    (
        '金华市',
        'jh',
        '浙江省',
        '330700',
        119.649506,
        29.089524,
        1,
        33
    ),
    (
        '台州市',
        'taizhou_zj',
        '浙江省',
        '331000',
        121.428599,
        28.661378,
        1,
        34
    ),
    -- 山东省续
    (
        '烟台市',
        'yt',
        '山东省',
        '370600',
        121.391382,
        37.539297,
        1,
        35
    ),
    (
        '潍坊市',
        'wf',
        '山东省',
        '370700',
        119.107078,
        36.70925,
        1,
        36
    ),
    (
        '淄博市',
        'zb',
        '山东省',
        '370300',
        118.047648,
        36.814939,
        1,
        37
    ),
    (
        '威海市',
        'wh',
        '山东省',
        '371000',
        122.116394,
        37.509691,
        1,
        38
    ),
    (
        '临沂市',
        'ly',
        '山东省',
        '371300',
        118.326443,
        35.065282,
        1,
        39
    ),
    -- 其他重要城市
    (
        '成都市',
        'cd',
        '四川省',
        '510100',
        104.065735,
        30.659462,
        1,
        40
    ),
    (
        '武汉市',
        'wuhan',
        '湖北省',
        '420100',
        114.298572,
        30.584355,
        1,
        41
    ),
    (
        '西安市',
        'xa',
        '陕西省',
        '610100',
        108.948024,
        34.263161,
        1,
        42
    ),
    (
        '郑州市',
        'zz',
        '河南省',
        '410100',
        113.665412,
        34.757975,
        1,
        43
    ),
    (
        '长沙市',
        'cs',
        '湖南省',
        '430100',
        112.982279,
        28.19409,
        1,
        44
    ),
    (
        '昆明市',
        'km',
        '云南省',
        '530100',
        102.712251,
        25.040609,
        1,
        45
    ),
    (
        '南宁市',
        'nn',
        '广西壮族自治区',
        '450100',
        108.320004,
        22.82402,
        1,
        46
    ),
    (
        '福州市',
        'fz',
        '福建省',
        '350100',
        119.306239,
        26.075302,
        1,
        47
    ),
    (
        '厦门市',
        'xm',
        '福建省',
        '350200',
        118.11022,
        24.490474,
        1,
        48
    ),
    (
        '合肥市',
        'hf',
        '安徽省',
        '340100',
        117.283042,
        31.86119,
        1,
        49
    ),
    (
        '南昌市',
        'nc',
        '江西省',
        '360100',
        115.892151,
        28.676493,
        1,
        50
    );

-- 插入行程测试数据
INSERT INTO
    trips (
        id,
        driver_id,
        driver_name,
        driver_avatar,
        start_location,
        end_location,
        departure_time,
        available_seats,
        price,
        vehicle_info,
        type,
        status,
        note
    )
VALUES
    -- 今天的行程
    (
        'trip_001',
        'driver_001',
        '张师傅',
        '/static/avatar1.png',
        '荣盛阿尔卡迪亚·花语城七地块',
        '北京市建国门',
        DATE_ADD(NOW(), INTERVAL 2 HOUR),
        3,
        38.00,
        '特斯拉 Model 3 白色',
        'car_seeking_people',
        'available',
        '准时出发'
    ),
    (
        'trip_002',
        'driver_002',
        '李师傅',
        '/static/avatar2.png',
        '北京市建国门',
        '廊坊市荣盛阿尔卡迪亚',
        DATE_ADD(NOW(), INTERVAL 3 HOUR),
        2,
        42.00,
        '比亚迪 汉 黑色',
        'car_seeking_people',
        'available',
        '高速直达'
    ),
    (
        'trip_003',
        'driver_003',
        '王师傅',
        '/static/avatar3.png',
        '永安里地铁站',
        '廊坊市香河',
        DATE_SUB(NOW(), INTERVAL 1 HOUR),
        1,
        35.00,
        '大众 朗逸 蓝色',
        'car_seeking_people',
        'completed',
        '已完成'
    ),
    -- 明天的行程
    (
        'trip_004',
        'driver_004',
        '赵师傅',
        '/static/avatar4.png',
        '荣盛阿尔卡迪亚·花语城',
        '朝阳门地铁站',
        DATE_ADD(
            DATE_ADD(CURDATE(), INTERVAL 1 DAY),
            INTERVAL 8 HOUR
        ),
        3,
        25.00,
        '白色 宝马3系',
        'car_seeking_people',
        'available',
        '早高峰出行'
    ),
    (
        'trip_005',
        'driver_005',
        '孙女士',
        '/static/avatar5.png',
        '金台夕照地铁站',
        '荣盛阿尔卡迪亚·花语城',
        DATE_ADD(
            DATE_ADD(CURDATE(), INTERVAL 1 DAY),
            INTERVAL 18 HOUR
        ),
        2,
        30.00,
        '黑色 奔驰C级',
        'car_seeking_people',
        'available',
        '晚高峰返程'
    );