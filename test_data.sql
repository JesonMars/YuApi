-- 创建测试数据：用户和行程数据
USE yugo_db;

-- 清除已有测试数据
DELETE FROM trip_participants WHERE trip_id LIKE 'test-%';
DELETE FROM trips WHERE id LIKE 'test-%';
DELETE FROM users WHERE id LIKE 'test-%';

-- 插入测试用户数据
INSERT INTO users (id, name, phone, avatar, community, vehicle_brand, vehicle_color, plate_number, balance, verification_status, wechat_openid, first_setup_completed, create_time) VALUES
('test-user-001', '张师傅', '13800138001', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '本田', '白色', '京A12345', 100.00, 'verified', 'wx_test_001', 1, NOW()),
('test-user-002', '李女士', '13800138002', 'https://via.placeholder.com/60', '万科城市花园', '丰田', '黑色', '京B67890', 80.50, 'verified', 'wx_test_002', 1, NOW()),
('test-user-003', '王先生', '13800138003', 'https://via.placeholder.com/60', '保利·香槟国际', '大众', '银色', '京C11111', 150.00, 'verified', 'wx_test_003', 1, NOW()),
('test-user-004', '刘师傅', '13800138004', 'https://via.placeholder.com/60', '金地格林小镇', '别克', '蓝色', '京D22222', 200.00, 'verified', 'wx_test_004', 1, NOW()),
('test-user-005', '陈女士', '13800138005', 'https://via.placeholder.com/60', '融侨·观邸', '奥迪', '红色', '京E33333', 120.75, 'verified', 'wx_test_005', 1, NOW()),
('test-user-006', '赵先生', '13800138006', 'https://via.placeholder.com/60', '华润·橡树湾', '宝马', '白色', '京F44444', 300.00, 'verified', 'wx_test_006', 1, NOW()),
('test-user-007', '孙女士', '13800138007', 'https://via.placeholder.com/60', '绿地·香颂', '奔驰', '黑色', '京G55555', 250.25, 'verified', 'wx_test_007', 1, NOW()),
('test-user-008', '周师傅', '13800138008', 'https://via.placeholder.com/60', '万达广场', '吉利', '灰色', '京H66666', 90.00, 'verified', 'wx_test_008', 1, NOW());

-- 插入测试行程数据（顺风车 - 车找人）
INSERT INTO trips (id, driver_id, driver_name, driver_avatar, start_location, end_location, departure_time, available_seats, passenger_count, price, vehicle_info, note, type, status, create_time) VALUES
-- 今天的行程
('test-trip-001', 'test-user-001', '张师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '永安里地铁站', DATE_ADD(NOW(), INTERVAL 1 HOUR), 3, 0, 15.00, '白色 本田雅阁', '车内禁烟，请系好安全带', 'car_seeking_people', 'available', NOW()),
('test-trip-002', 'test-user-002', '李女士', 'https://via.placeholder.com/60', '建国门地铁站', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 2 HOUR), 2, 0, 12.00, '黑色 丰田凯美瑞', '准时出发，不等人', 'car_seeking_people', 'available', NOW()),
('test-trip-003', 'test-user-003', '王先生', 'https://via.placeholder.com/60', '国贸CBD', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 3 HOUR), 1, 0, 18.00, '银色 大众帕萨特', '可在途中接人', 'car_seeking_people', 'available', NOW()),
('test-trip-004', 'test-user-004', '刘师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '北京站', DATE_ADD(NOW(), INTERVAL 4 HOUR), 4, 1, 20.00, '蓝色 别克君越', '舒适商务车', 'car_seeking_people', 'available', NOW()),
('test-trip-005', 'test-user-005', '陈女士', 'https://via.placeholder.com/60', '东大桥地铁站', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 5 HOUR), 2, 0, 16.00, '红色 奥迪A4L', '女司机，安全驾驶', 'car_seeking_people', 'available', NOW()),

-- 明天的行程
('test-trip-006', 'test-user-006', '赵先生', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '朝阳门地铁站', DATE_ADD(NOW(), INTERVAL 1 DAY), 3, 0, 14.00, '白色 宝马3系', '豪华轿车，舒适体验', 'car_seeking_people', 'available', NOW()),
('test-trip-007', 'test-user-007', '孙女士', 'https://via.placeholder.com/60', '金台夕照地铁站', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 0, 17.00, '黑色 奔驰C级', '高端商务出行', 'car_seeking_people', 'available', NOW()),
('test-trip-008', 'test-user-008', '周师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '广渠门外地铁站', DATE_ADD(NOW(), INTERVAL 1 DAY), 3, 0, 13.00, '灰色 吉利帝豪', '经济实惠，准点出发', 'car_seeking_people', 'available', NOW()),

-- 拼车求搭车的行程
('test-trip-009', 'test-user-001', '张师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '天安门东地铁站', DATE_ADD(NOW(), INTERVAL 6 HOUR), 0, 1, 0.00, '', '需要搭车，可分摊油费', 'people_seeking_car', 'available', NOW()),
('test-trip-010', 'test-user-003', '王先生', 'https://via.placeholder.com/60', '西单地铁站', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 7 HOUR), 0, 1, 0.00, '', '求搭车回家，可AA油费', 'people_seeking_car', 'available', NOW());

-- 更新统计数据
UPDATE trips SET passenger_count = 1 WHERE id = 'test-trip-004';