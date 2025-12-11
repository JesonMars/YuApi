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
}