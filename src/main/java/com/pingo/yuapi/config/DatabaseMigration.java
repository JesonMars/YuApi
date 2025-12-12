package com.pingo.yuapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库迁移配置
 * 用于自动添加缺失的数据库字段
 */
@Component
public class DatabaseMigration implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            // 添加 first_setup_completed 字段（如果不存在）
            addFirstSetupCompletedColumn();
            
            // 添加坐标字段到trips表（如果不存在）
            addTripCoordinateColumns();
            
            // 添加测试数据（如果不存在）
            addTestData();
            
            // 添加测试关注数据
            addTestFollowData();
        } catch (Exception e) {
            logger.error("数据库迁移失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 添加 first_setup_completed 字段
     */
    private void addFirstSetupCompletedColumn() {
        try {
            // 检查字段是否已存在
            String checkColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'yugo_db' 
                AND TABLE_NAME = 'users' 
                AND COLUMN_NAME = 'first_setup_completed'
                """;
            
            Integer count = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);
            
            if (count == null || count == 0) {
                // 字段不存在，添加字段
                String addColumnSql = """
                    ALTER TABLE users 
                    ADD COLUMN first_setup_completed TINYINT DEFAULT 0 
                    COMMENT '是否完成首次设置：0-未完成，1-已完成'
                    """;
                
                jdbcTemplate.execute(addColumnSql);
                logger.info("成功添加 first_setup_completed 字段到 users 表");
                
                // 添加索引
                String addIndexSql = "CREATE INDEX idx_first_setup_completed ON users(first_setup_completed)";
                try {
                    jdbcTemplate.execute(addIndexSql);
                    logger.info("成功创建 first_setup_completed 字段索引");
                } catch (Exception e) {
                    logger.warn("创建索引失败（可能已存在）: {}", e.getMessage());
                }
                
            } else {
                logger.info("first_setup_completed 字段已存在，跳过迁移");
            }
            
        } catch (Exception e) {
            logger.error("添加 first_setup_completed 字段失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 添加坐标字段到trips表
     */
    private void addTripCoordinateColumns() {
        try {
            // 检查起点经度字段是否已存在
            String checkColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'yugo_db' 
                AND TABLE_NAME = 'trips' 
                AND COLUMN_NAME = 'start_longitude'
                """;
            
            Integer count = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);
            
            if (count == null || count == 0) {
                // 添加起点和终点坐标字段
                String[] alterSqls = {
                    "ALTER TABLE trips ADD COLUMN start_longitude DECIMAL(10,7) COMMENT '起点经度'",
                    "ALTER TABLE trips ADD COLUMN start_latitude DECIMAL(10,7) COMMENT '起点纬度'",
                    "ALTER TABLE trips ADD COLUMN end_longitude DECIMAL(10,7) COMMENT '终点经度'",
                    "ALTER TABLE trips ADD COLUMN end_latitude DECIMAL(10,7) COMMENT '终点纬度'"
                };
                
                for (String sql : alterSqls) {
                    jdbcTemplate.execute(sql);
                }
                
                logger.info("成功添加坐标字段到 trips 表");
                
                // 添加空间索引
                try {
                    jdbcTemplate.execute("CREATE INDEX idx_start_location ON trips(start_longitude, start_latitude)");
                    jdbcTemplate.execute("CREATE INDEX idx_end_location ON trips(end_longitude, end_latitude)");
                    logger.info("成功创建位置坐标索引");
                } catch (Exception e) {
                    logger.warn("创建位置索引失败（可能已存在）: {}", e.getMessage());
                }
                
                // 为现有数据添加默认坐标（北京地区）
                updateExistingTripsWithCoordinates();
                
            } else {
                logger.info("坐标字段已存在，跳过迁移");
            }
            
        } catch (Exception e) {
            logger.error("添加坐标字段失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 为现有行程数据添加坐标信息
     */
    private void updateExistingTripsWithCoordinates() {
        try {
            // 为常见地点添加坐标
            String[] updateSqls = {
                // 荣盛阿尔卡迪亚·花语城
                "UPDATE trips SET start_longitude = 116.7428, start_latitude = 39.5283 WHERE start_location LIKE '%荣盛%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.7428, end_latitude = 39.5283 WHERE end_location LIKE '%荣盛%' AND end_longitude IS NULL",
                
                // 永安里地铁站
                "UPDATE trips SET start_longitude = 116.4431, start_latitude = 39.9147 WHERE start_location LIKE '%永安里%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.4431, end_latitude = 39.9147 WHERE end_location LIKE '%永安里%' AND end_longitude IS NULL",
                
                // 建国门地铁站
                "UPDATE trips SET start_longitude = 116.4352, start_latitude = 39.9063 WHERE start_location LIKE '%建国门%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.4352, end_latitude = 39.9063 WHERE end_location LIKE '%建国门%' AND end_longitude IS NULL",
                
                // 国贸CBD
                "UPDATE trips SET start_longitude = 116.4609, start_latitude = 39.9081 WHERE start_location LIKE '%国贸%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.4609, end_latitude = 39.9081 WHERE end_location LIKE '%国贸%' AND end_longitude IS NULL",
                
                // 北京站
                "UPDATE trips SET start_longitude = 116.4267, start_latitude = 39.9031 WHERE start_location LIKE '%北京站%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.4267, end_latitude = 39.9031 WHERE end_location LIKE '%北京站%' AND end_longitude IS NULL",
                
                // 朝阳门地铁站
                "UPDATE trips SET start_longitude = 116.4342, start_latitude = 39.9242 WHERE start_location LIKE '%朝阳门%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.4342, end_latitude = 39.9242 WHERE end_location LIKE '%朝阳门%' AND end_longitude IS NULL",
                
                // 中关村软件园
                "UPDATE trips SET start_longitude = 116.2923, start_latitude = 40.0434 WHERE start_location LIKE '%中关村%软件园%' AND start_longitude IS NULL",
                "UPDATE trips SET end_longitude = 116.2923, end_latitude = 40.0434 WHERE end_location LIKE '%中关村%软件园%' AND end_longitude IS NULL"
            };
            
            for (String sql : updateSqls) {
                int updated = jdbcTemplate.update(sql);
                if (updated > 0) {
                    logger.info("更新了 {} 条行程坐标数据", updated);
                }
            }
            
        } catch (Exception e) {
            logger.error("更新现有行程坐标失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 添加测试数据
     */
    private void addTestData() {
        try {
            // 检查是否已有测试数据
            String checkSql = "SELECT COUNT(*) FROM users WHERE id LIKE 'test-%'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                // 插入测试用户数据
                insertTestUsers();
                // 插入测试行程数据
                insertTestTrips();
                logger.info("成功添加测试数据");
            } else {
                logger.info("测试数据已存在，跳过添加");
            }
        } catch (Exception e) {
            logger.error("添加测试数据失败: {}", e.getMessage(), e);
        }
    }
    
    private void insertTestUsers() {
        String[] userSqls = {
            "INSERT INTO users (id, name, phone, avatar, community, vehicle_brand, vehicle_color, plate_number, balance, verification_status, wechat_openid, first_setup_completed) VALUES " +
            "('test-user-001', '张师傅', '13800138001', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '本田', '白色', '京A12345', 100.00, 'verified', 'wx_test_001', 1)",
            
            "INSERT INTO users (id, name, phone, avatar, community, vehicle_brand, vehicle_color, plate_number, balance, verification_status, wechat_openid, first_setup_completed) VALUES " +
            "('test-user-002', '李女士', '13800138002', 'https://via.placeholder.com/60', '万科城市花园', '丰田', '黑色', '京B67890', 80.50, 'verified', 'wx_test_002', 1)",
            
            "INSERT INTO users (id, name, phone, avatar, community, vehicle_brand, vehicle_color, plate_number, balance, verification_status, wechat_openid, first_setup_completed) VALUES " +
            "('test-user-003', '王先生', '13800138003', 'https://via.placeholder.com/60', '保利·香槟国际', '大众', '银色', '京C11111', 150.00, 'verified', 'wx_test_003', 1)",
            
            "INSERT INTO users (id, name, phone, avatar, community, vehicle_brand, vehicle_color, plate_number, balance, verification_status, wechat_openid, first_setup_completed) VALUES " +
            "('test-user-004', '刘师傅', '13800138004', 'https://via.placeholder.com/60', '金地格林小镇', '别克', '蓝色', '京D22222', 200.00, 'verified', 'wx_test_004', 1)"
        };
        
        for (String sql : userSqls) {
            jdbcTemplate.execute(sql);
        }
    }
    
    private void insertTestTrips() {
        String[] tripSqls = {
            "INSERT INTO trips (id, driver_id, driver_name, driver_avatar, start_location, end_location, departure_time, available_seats, passenger_count, price, vehicle_info, note, type, status) VALUES " +
            "('test-trip-001', 'test-user-001', '张师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '永安里地铁站', DATE_ADD(NOW(), INTERVAL 1 HOUR), 3, 0, 15.00, '白色 本田雅阁', '车内禁烟，请系好安全带', 'car_seeking_people', 'available')",
            
            "INSERT INTO trips (id, driver_id, driver_name, driver_avatar, start_location, end_location, departure_time, available_seats, passenger_count, price, vehicle_info, note, type, status) VALUES " +
            "('test-trip-002', 'test-user-002', '李女士', 'https://via.placeholder.com/60', '建国门地铁站', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 2 HOUR), 2, 0, 12.00, '黑色 丰田凯美瑞', '准时出发，不等人', 'car_seeking_people', 'available')",
            
            "INSERT INTO trips (id, driver_id, driver_name, driver_avatar, start_location, end_location, departure_time, available_seats, passenger_count, price, vehicle_info, note, type, status) VALUES " +
            "('test-trip-003', 'test-user-003', '王先生', 'https://via.placeholder.com/60', '国贸CBD', '荣盛阿尔卡迪亚·花语城', DATE_ADD(NOW(), INTERVAL 3 HOUR), 1, 0, 18.00, '银色 大众帕萨特', '可在途中接人', 'car_seeking_people', 'available')",
            
            "INSERT INTO trips (id, driver_id, driver_name, driver_avatar, start_location, end_location, departure_time, available_seats, passenger_count, price, vehicle_info, note, type, status) VALUES " +
            "('test-trip-004', 'test-user-004', '刘师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '北京站', DATE_ADD(NOW(), INTERVAL 4 HOUR), 4, 1, 20.00, '蓝色 别克君越', '舒适商务车', 'car_seeking_people', 'available')",
            
            // 明天的行程
            "INSERT INTO trips (id, driver_id, driver_name, driver_avatar, start_location, end_location, departure_time, available_seats, passenger_count, price, vehicle_info, note, type, status) VALUES " +
            "('test-trip-005', 'test-user-001', '张师傅', 'https://via.placeholder.com/60', '荣盛阿尔卡迪亚·花语城', '朝阳门地铁站', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 3, 0, 14.00, '白色 本田雅阁', '豪华轿车，舒适体验', 'car_seeking_people', 'available')"
        };
        
        for (String sql : tripSqls) {
            jdbcTemplate.execute(sql);
        }
    }
    
    /**
     * 添加测试关注数据
     */
    private void addTestFollowData() {
        try {
            // 检查是否已有关注数据
            String checkSql = "SELECT COUNT(*) FROM user_follows";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                // 先创建real_driver_001用户
                insertRealDriverUser();
                
                // 插入测试关注数据
                String[] followSqls = {
                    // 假设当前用户是test-user-001，他关注了其他几个司机
                    "INSERT INTO user_follows (id, user_id, target_user_id) VALUES " +
                    "('follow-001', 'test-user-001', 'test-user-002')",  // 张师傅关注李女士
                    
                    "INSERT INTO user_follows (id, user_id, target_user_id) VALUES " +
                    "('follow-002', 'test-user-001', 'test-user-003')",  // 张师傅关注王先生
                    
                    "INSERT INTO user_follows (id, user_id, target_user_id) VALUES " +
                    "('follow-003', 'test-user-001', 'real_driver_001')" // 张师傅关注数据库测试司机
                };
                
                for (String sql : followSqls) {
                    jdbcTemplate.execute(sql);
                }
                logger.info("成功添加测试关注数据");
            } else {
                logger.info("测试关注数据已存在，跳过添加");
            }
        } catch (Exception e) {
            logger.error("添加测试关注数据失败: {}", e.getMessage(), e);
        }
    }
    
    private void insertRealDriverUser() {
        try {
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = 'real_driver_001'";
            Integer userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class);
            
            if (userCount == null || userCount == 0) {
                String insertUserSql = "INSERT INTO users (id, name, phone, avatar, community, vehicle_brand, vehicle_color, plate_number, balance, verification_status, wechat_openid, first_setup_completed) VALUES " +
                    "('real_driver_001', '数据库测试司机', '13800138005', '/static/db_avatar.png', '软件园小区', '特斯拉', '红色', '京E88888', 300.00, 'verified', 'wx_test_005', 1)";
                jdbcTemplate.execute(insertUserSql);
                logger.info("成功创建real_driver_001用户");
            }
        } catch (Exception e) {
            logger.error("创建real_driver_001用户失败: {}", e.getMessage(), e);
        }
    }
}