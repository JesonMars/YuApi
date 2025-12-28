package com.pingo.yuapi.dto;

import java.util.Map;

/**
 * 订单支付响应DTO
 */
public class OrderPaymentResponse {
    private String orderId;
    private String outTradeNo;
    private Map<String, Object> paymentParams; // 微信支付参数

    public OrderPaymentResponse() {}

    public OrderPaymentResponse(String orderId, String outTradeNo, Map<String, Object> paymentParams) {
        this.orderId = orderId;
        this.outTradeNo = outTradeNo;
        this.paymentParams = paymentParams;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Map<String, Object> getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(Map<String, Object> paymentParams) {
        this.paymentParams = paymentParams;
    }
}
