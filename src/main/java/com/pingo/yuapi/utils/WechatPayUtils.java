package com.pingo.yuapi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 微信支付工具类
 */
public class WechatPayUtils {

    private static final Logger logger = LoggerFactory.getLogger(WechatPayUtils.class);

    // 微信支付配置（应该从配置文件或数据库读取）
    private static final String APP_ID = "your_app_id"; // 小程序AppID
    private static final String MCH_ID = "your_mch_id"; // 服务商商户号
    private static final String SUB_MCH_ID = "your_sub_mch_id"; // 子商户号（可选，用于服务商模式）
    private static final String API_KEY = "your_api_key"; // API密钥
    private static final String NOTIFY_URL = "https://your-domain.com/api/payment/wechat/notify"; // 支付回调地址
    private static final String PROFIT_SHARING_NOTIFY_URL = "https://your-domain.com/api/payment/wechat/profit-sharing-notify"; // 分账回调地址

    /**
     * 生成微信小程序支付参数
     *
     * @param outTradeNo 商户订单号
     * @param totalFee 支付金额（单位：分）
     * @param body 商品描述
     * @param openid 用户openid
     * @return 微信支付参数
     */
    public static Map<String, Object> generateJsapiPayParams(String outTradeNo, int totalFee, String body, String openid) {
        try {
            // 1. 构建统一下单请求参数
            Map<String, String> params = new TreeMap<>();
            params.put("appid", APP_ID);
            params.put("mch_id", MCH_ID);
            params.put("nonce_str", generateNonceStr());
            params.put("body", body);
            params.put("out_trade_no", outTradeNo);
            params.put("total_fee", String.valueOf(totalFee));
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", NOTIFY_URL);
            params.put("trade_type", "JSAPI");
            params.put("openid", openid);

            // 2. 生成签名
            String sign = generateSign(params, API_KEY);
            params.put("sign", sign);

            // 3. 调用微信统一下单接口（这里简化处理，实际需要调用微信API）
            // String prepayId = callWechatUnifiedOrder(params);
            String prepayId = "mock_prepay_id_" + System.currentTimeMillis();

            // 4. 生成小程序支付参数
            Map<String, Object> payParams = new TreeMap<>();
            payParams.put("appId", APP_ID);
            payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            payParams.put("nonceStr", generateNonceStr());
            payParams.put("package", "prepay_id=" + prepayId);
            payParams.put("signType", "MD5");

            // 生成支付签名
            Map<String, String> signParams = new TreeMap<>();
            signParams.put("appId", (String) payParams.get("appId"));
            signParams.put("timeStamp", (String) payParams.get("timeStamp"));
            signParams.put("nonceStr", (String) payParams.get("nonceStr"));
            signParams.put("package", (String) payParams.get("package"));
            signParams.put("signType", (String) payParams.get("signType"));

            String paySign = generateSign(signParams, API_KEY);
            payParams.put("paySign", paySign);

            return payParams;

        } catch (Exception e) {
            logger.error("生成微信支付参数失败", e);
            return null;
        }
    }

    /**
     * 生成随机字符串
     */
    private static String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成签名
     */
    private static String generateSign(Map<String, String> params, String apiKey) {
        try {
            // 1. 参数按字典序排序
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }

            // 2. 拼接API密钥
            String stringSignTemp = sb.append("key=").append(apiKey).toString();

            // 3. MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(stringSignTemp.getBytes("UTF-8"));

            // 4. 转换为大写
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();

        } catch (Exception e) {
            logger.error("生成签名失败", e);
            return null;
        }
    }

    /**
     * 验证微信支付回调签名
     */
    public static boolean verifyNotifySign(Map<String, String> params) {
        String sign = params.get("sign");
        if (sign == null || sign.isEmpty()) {
            return false;
        }

        // 移除sign参数
        Map<String, String> signParams = new TreeMap<>(params);
        signParams.remove("sign");

        // 重新生成签名并比较
        String newSign = generateSign(signParams, API_KEY);
        return sign.equals(newSign);
    }

    /**
     * 构建微信支付回调响应XML
     */
    public static String buildNotifyResponse(boolean success, String msg) {
        if (success) {
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } else {
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
        }
    }

    /**
     * 请求分账
     *
     * @param transactionId 微信订单号
     * @param outOrderNo 商户分账单号
     * @param receivers 分账接收方列表 [{type, account, amount, description}]
     * @return 分账结果
     */
    public static Map<String, Object> requestProfitSharing(String transactionId, String outOrderNo, List<Map<String, Object>> receivers) {
        try {
            // 1. 构建分账请求参数
            Map<String, String> params = new TreeMap<>();
            params.put("mch_id", MCH_ID);
            params.put("appid", APP_ID);
            params.put("nonce_str", generateNonceStr());
            params.put("transaction_id", transactionId);
            params.put("out_order_no", outOrderNo);

            // 构建分账接收方JSON
            JSONObject receiversJson = new JSONObject();
            receiversJson.put("receivers", receivers);
            params.put("receivers", receiversJson.toJSONString());

            // 2. 生成签名
            String sign = generateSign(params, API_KEY);
            params.put("sign", sign);

            // 3. 调用微信分账接口（这里简化处理，实际需要调用微信API）
            // String result = httpPost("https://api.mch.weixin.qq.com/secapi/pay/profitsharing", params);

            // 模拟返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("return_code", "SUCCESS");
            result.put("result_code", "SUCCESS");
            result.put("transaction_id", transactionId);
            result.put("out_order_no", outOrderNo);
            result.put("order_id", "mock_profit_sharing_order_" + System.currentTimeMillis());

            logger.info("请求分账成功: transactionId={}, outOrderNo={}", transactionId, outOrderNo);

            return result;

        } catch (Exception e) {
            logger.error("请求分账失败", e);
            return null;
        }
    }

    /**
     * 查询分账结果
     *
     * @param transactionId 微信订单号
     * @param outOrderNo 商户分账单号
     * @return 分账结果
     */
    public static Map<String, Object> queryProfitSharing(String transactionId, String outOrderNo) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("mch_id", MCH_ID);
            params.put("transaction_id", transactionId);
            params.put("out_order_no", outOrderNo);
            params.put("nonce_str", generateNonceStr());

            String sign = generateSign(params, API_KEY);
            params.put("sign", sign);

            // 调用微信查询分账接口
            // String result = httpPost("https://api.mch.weixin.qq.com/pay/profitsharingquery", params);

            // 模拟返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("return_code", "SUCCESS");
            result.put("result_code", "SUCCESS");
            result.put("status", "FINISHED"); // PROCESSING(处理中), FINISHED(分账完成)

            return result;

        } catch (Exception e) {
            logger.error("查询分账结果失败", e);
            return null;
        }
    }

    /**
     * 完结分账（表示此订单不再进行分账）
     *
     * @param transactionId 微信订单号
     * @param outOrderNo 商户分账单号
     * @param description 描述
     * @return 是否成功
     */
    public static boolean finishProfitSharing(String transactionId, String outOrderNo, String description) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("mch_id", MCH_ID);
            params.put("appid", APP_ID);
            params.put("nonce_str", generateNonceStr());
            params.put("transaction_id", transactionId);
            params.put("out_order_no", outOrderNo);
            params.put("description", description);

            String sign = generateSign(params, API_KEY);
            params.put("sign", sign);

            // 调用微信完结分账接口
            // String result = httpPost("https://api.mch.weixin.qq.com/secapi/pay/profitsharingfinish", params);

            logger.info("完结分账成功: transactionId={}, outOrderNo={}", transactionId, outOrderNo);

            return true;

        } catch (Exception e) {
            logger.error("完结分账失败", e);
            return false;
        }
    }

    /**
     * 添加分账接收方
     *
     * @param type 分账接收方类型：MERCHANT_ID(商户号), PERSONAL_OPENID(个人openid)
     * @param account 分账接收方账户
     * @param name 分账接收方姓名
     * @param relationType 与分账方的关系类型
     * @return 是否成功
     */
    public static boolean addProfitSharingReceiver(String type, String account, String name, String relationType) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("mch_id", MCH_ID);
            params.put("appid", APP_ID);
            params.put("nonce_str", generateNonceStr());

            JSONObject receiver = new JSONObject();
            receiver.put("type", type);
            receiver.put("account", account);
            receiver.put("name", name);
            receiver.put("relation_type", relationType);

            params.put("receiver", receiver.toJSONString());

            String sign = generateSign(params, API_KEY);
            params.put("sign", sign);

            // 调用微信添加分账接收方接口
            // String result = httpPost("https://api.mch.weixin.qq.com/pay/profitsharingaddreceiver", params);

            logger.info("添加分账接收方成功: type={}, account={}", type, account);

            return true;

        } catch (Exception e) {
            logger.error("添加分账接收方失败", e);
            return false;
        }
    }
}
