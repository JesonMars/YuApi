-- 为trips表添加新字段（如果字段不存在）

-- 添加城市字段
ALTER TABLE trips ADD COLUMN IF NOT EXISTS start_city VARCHAR(50) COMMENT '起点城市' AFTER start_location;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS end_city VARCHAR(50) COMMENT '终点城市' AFTER end_location;

-- 添加途经点字段
ALTER TABLE trips ADD COLUMN IF NOT EXISTS pickup_point TEXT COMMENT '上车点（JSON数组）' AFTER end_latitude;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS dropoff_point TEXT COMMENT '下车点（JSON数组）' AFTER pickup_point;

-- 添加价格字段
ALTER TABLE trips ADD COLUMN IF NOT EXISTS price_per_seat DECIMAL(8, 2) COMMENT '单价/位' AFTER price;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS total_income DECIMAL(8, 2) COMMENT '总收入' AFTER price_per_seat;

-- 添加循环字段
ALTER TABLE trips ADD COLUMN IF NOT EXISTS recurring BOOLEAN DEFAULT FALSE COMMENT '是否循环' AFTER note;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS recurring_type VARCHAR(20) COMMENT '循环类型：仅当次/每周重复/每月重复' AFTER recurring;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS timing VARCHAR(20) COMMENT '时段：tonight-今晚/tomorrow-明早' AFTER recurring_type;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_timing ON trips(timing);
