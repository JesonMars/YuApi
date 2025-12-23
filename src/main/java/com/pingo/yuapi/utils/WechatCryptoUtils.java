package com.pingo.yuapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 微信小程序数据解密工具类
 */
public class WechatCryptoUtils {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AES解密
     * @param data 加密的数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解密后的数据
     */
    public static String decrypt(String data, String sessionKey, String iv) {
        try {
            // Base64解码
            byte[] encrypted = Base64.decodeBase64(data);
            byte[] sessionKeyBytes = Base64.decodeBase64(sessionKey);
            byte[] ivBytes = Base64.decodeBase64(iv);

            // AES解密
            SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKeyBytes, ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            
            // 去除填充
            String result = new String(decrypted, StandardCharsets.UTF_8);
            
            // 去除可能的padding字符
            int index = result.lastIndexOf('}');
            if (index != -1) {
                result = result.substring(0, index + 1);
            }
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密用户信息
     * @param encryptedData 加密的用户数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 用户信息Map
     */
    public static Map<String, Object> decryptUserInfo(String encryptedData, String sessionKey, String iv) {
        try {
            String decryptedData = decrypt(encryptedData, sessionKey, iv);
            return objectMapper.readValue(decryptedData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解密用户信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密手机号信息
     * @param encryptedData 加密的手机号数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 手机号信息Map
     */
    public static Map<String, Object> decryptPhoneNumber(String encryptedData, String sessionKey, String iv) {
        try {
            String decryptedData = decrypt(encryptedData, sessionKey, iv);
            return objectMapper.readValue(decryptedData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解密手机号失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密运动数据（如果需要）
     * @param encryptedData 加密的运动数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 运动数据Map
     */
    public static Map<String, Object> decryptRunData(String encryptedData, String sessionKey, String iv) {
        try {
            String decryptedData = decrypt(encryptedData, sessionKey, iv);
            return objectMapper.readValue(decryptedData, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("解密运动数据失败: " + e.getMessage(), e);
        }
    }
}