package com.pingo.yuapi.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 * 统一使用"分"作为存储单位，前端交互使用"元"
 *
 * 设计原则：
 * 1. 数据库存储：BIGINT，单位为分
 * 2. Java内部：Long类型，单位为分
 * 3. API边界：前后端交互用元（Double），内部立即转为分
 */
public class MoneyUtils {

    /**
     * 货币单位：1元 = 100分
     */
    private static final long MONEY_UNIT = 100;

    /**
     * 元转分（BigDecimal版本）
     * @param yuan 金额（元）
     * @return 金额（分）
     */
    public static Long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return null;
        }
        return yuan.multiply(new BigDecimal(MONEY_UNIT))
                   .setScale(0, RoundingMode.HALF_UP)
                   .longValue();
    }

    /**
     * 元转分（Double版本）
     * @param yuan 金额（元）
     * @return 金额（分）
     */
    public static Long yuanToFen(Double yuan) {
        if (yuan == null) {
            return null;
        }
        // 使用BigDecimal避免浮点精度问题
        return yuanToFen(BigDecimal.valueOf(yuan));
    }

    /**
     * 元转分（Integer版本）
     * @param yuan 金额（元）
     * @return 金额（分）
     */
    public static Long yuanToFen(Integer yuan) {
        if (yuan == null) {
            return null;
        }
        return yuan * MONEY_UNIT;
    }

    /**
     * 分转元（BigDecimal版本）
     * @param fen 金额（分）
     * @return 金额（元），保留2位小数
     */
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return null;
        }
        return new BigDecimal(fen)
                .divide(new BigDecimal(MONEY_UNIT), 2, RoundingMode.HALF_UP);
    }

    /**
     * 分转元（Double版本）
     * @param fen 金额（分）
     * @return 金额（元）
     */
    public static Double fenToYuanDouble(Long fen) {
        if (fen == null) {
            return null;
        }
        return fen / (double) MONEY_UNIT;
    }

    /**
     * 分转元（String版本，适合前端展示）
     * @param fen 金额（分）
     * @return 金额（元），格式化为2位小数字符串
     */
    public static String fenToYuanString(Long fen) {
        if (fen == null) {
            return "0.00";
        }
        return fenToYuan(fen).toPlainString();
    }

    /**
     * 格式化金额（分）为货币显示格式
     * @param fen 金额（分）
     * @return 格式化字符串，如：¥20.50
     */
    public static String formatMoney(Long fen) {
        if (fen == null) {
            return "¥0.00";
        }
        return "¥" + fenToYuanString(fen);
    }

    /**
     * 计算总价（分）
     * @param unitPrice 单价（分）
     * @param quantity 数量
     * @return 总价（分）
     */
    public static Long calculateTotal(Long unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null) {
            return 0L;
        }
        return unitPrice * quantity;
    }

    /**
     * 验证金额是否有效（非负）
     * @param fen 金额（分）
     * @return 是否有效
     */
    public static boolean isValidAmount(Long fen) {
        return fen != null && fen >= 0;
    }

    /**
     * 验证金额是否在指定范围内
     * @param fen 金额（分）
     * @param minFen 最小值（分）
     * @param maxFen 最大值（分）
     * @return 是否在范围内
     */
    public static boolean isInRange(Long fen, Long minFen, Long maxFen) {
        if (fen == null) {
            return false;
        }
        return fen >= minFen && fen <= maxFen;
    }

    /**
     * 元字符串转分
     * @param yuanStr 金额字符串（元），如"20.50"
     * @return 金额（分）
     */
    public static Long yuanStrToFen(String yuanStr) {
        if (yuanStr == null || yuanStr.trim().isEmpty()) {
            return null;
        }
        try {
            BigDecimal yuan = new BigDecimal(yuanStr);
            return yuanToFen(yuan);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 比较两个金额的大小
     * @param fen1 金额1（分）
     * @param fen2 金额2（分）
     * @return 1: fen1>fen2, 0: fen1==fen2, -1: fen1<fen2
     */
    public static int compare(Long fen1, Long fen2) {
        if (fen1 == null && fen2 == null) {
            return 0;
        }
        if (fen1 == null) {
            return -1;
        }
        if (fen2 == null) {
            return 1;
        }
        return Long.compare(fen1, fen2);
    }
}
