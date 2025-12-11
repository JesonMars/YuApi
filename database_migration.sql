-- 数据库迁移脚本：添加 first_setup_completed 字段
-- 如果字段不存在则添加

-- 检查并添加 first_setup_completed 字段
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS first_setup_completed TINYINT DEFAULT 0 COMMENT '是否完成首次设置：0-未完成，1-已完成';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_first_setup_completed ON users(first_setup_completed);

-- 更新现有用户的初始值（可选，默认为0）
-- UPDATE users SET first_setup_completed = 0 WHERE first_setup_completed IS NULL;