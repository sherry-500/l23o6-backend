package org.fffd.l23o6.pojo.enum_;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OrderStatus {
    @JsonProperty("等待支付") PENDING_PAYMENT("等待支付"), @JsonProperty("alipay已支付") PAID("alipay已支付"),  @JsonProperty("other已支付") OPAID("other已支付"), @JsonProperty("已取消") CANCELLED("已取消"), @JsonProperty("已完成") COMPLETED("已完成");

    public String text;

    OrderStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
