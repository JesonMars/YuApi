-- 途经点功能数据库迁移脚本
-- 执行日期: 2025-12-17
-- 功能说明: 为途经点存储和缓存功能创建必要的数据库表

USE yugo_db;

-- 检查并创建用户通勤配置表（如果不存在）
CREATE TABLE IF NOT EXISTS user_commute_config (
    id VARCHAR(50) PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    timing VARCHAR(20) NOT NULL COMMENT '时间段: tonight-今晚, tomorrow-明早',
    pickup_points TEXT COMMENT '上车点JSON数组',
    dropoff_points TEXT COMMENT '下车点JSON数组',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_user_timing (user_id, timing) COMMENT '用户+时间段唯一索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '用户通勤配置表 - 存储用户的上下车点配置';

-- 查看表结构
DESCRIBE user_commute_config;

-- 查询现有数据
SELECT COUNT(*) as total_configs FROM user_commute_config;
