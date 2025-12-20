package com.pingo.yuapi.utils;

import java.util.UUID;

/**
 * ID生成工具类
 * 用于统一生成系统中的各种ID
 */
public class IdGeneratorUtils {

    /**
     * 生成标准的UUID字符串 (36位)
     * 
     * @return UUID字符串
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成不带横线的UUID字符串 (32位)
     * 
     * @return UUID字符串
     */
    public static String generateSimpleId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成短ID (8位)
     * 注意：截取UUID前8位，重复概率相对较高，仅用于对唯一性要求不高的场景或拼接前缀使用
     * 
     * @return 8位ID字符串
     */
    public static String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
