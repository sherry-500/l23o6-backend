package org.fffd.l23o6.util.strategy.payment;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.*;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import java.io.IOException;
import java.util.Map;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;

public class AliPayPaymentStrategy extends PaymentStrategy {
    public static final AliPayPaymentStrategy INSTANCE = new AliPayPaymentStrategy();
    private final class AlipayConfig{
        public static String APP_ID = "9021000122697460";
        //应用私钥
        public static String MERCHANT_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCgXA+8fOCuQCcUuoV7ThwVYULewIZ3SU17rRxIqZC72Nvxn6ak6EIylAJl354CuMccWBI9woZfi3LUfGL01b1N4NIi7+PvhIE72OpvBuKz12LesoFUEiJ9wnK+2aiURfs1CMX3IdnqpWD9lKzoyFB82rcluZy14ua8jMvZO2ApbLaTo0UCcBqBolfhCU2KmaL6iQFgqka3qqbwk+F+mMfIuZK08ChfbjbAfu3wYiGgLbtMCfkWSo/pfpI+U2rtQWvxmJtHn/um6B79ylGnp13AO+PIbTfcFZjSu2yjH6ct98ArMpBTc/4L4a3Wi0iygGYkx2SahBBAhnvORFwQ6oUXAgMBAAECggEBAItX9AShrss7y7njamH/OcAKSAUv3qiA9edmQZGSZsYUZurLBA+AEyXIH11Otws+bSZgEVnBUuprTlj9zS9bG6g0l+Tr93EF/xZ2IyBfJ1eG7c795gubxoTpFrRohmhxdxxah0t8CtQu6Nzw+F8+0VHs7ADIDTiv9jgZUHlvctwKy7iGu0TH5UU+FHoTMuMWZGp17xc0byLZwDLtTRGb4r6qcuCSS/pz3G06MbWvyy3iNCEFlFO3emYWRvdBEWiJBlpCfDvyZ1DBQjbL+cYHaEAuBZ3Vtms75smc8hOyjI0YF5IEL2hlSlAcP/BYnszvNpW0T0mM2hYAhOKWpVApT4ECgYEA6jLWt/btkIpkSF0bBBWBo3t2poN90mcQCI1IAyCC1I51SCyce7I/bgmcHrtW527Qcl2LRVvu1gvQNx2c3gLk0l/2fGHAtBnM3cpW9zLuVn9HcbaCzm1jDzxxJMLo0EfVwot1ZOHgjVOleWd+ibDww/7HU18jejPerqovA4NFrJECgYEAr0mS+ce5X2q4qt+MDPnYE6GxcleqHYEYeWWy+Tfxba/XWnfnlhtId2Xi53JRQrn2VTLHwQvZusak22rnYn2HIXT3oO2J0U9R8MUygplmO2tc0a1imQrtOCkuRGuoERHzSk4Ibt74m+eM7DfYOf3Go/FEGqntAP1OQmawM5IzCycCgYEAqWkTF0oguPDS7+GhvEeOWmLiBjuR1ZRF7dm9IL3wxa8zOX1DyNq8tW1wKG7dw1XOpRvztb11vBIZkPTz10kCmwddtsXDbsRHBJzsYa7O53T0ilY6svY/tPLE6ZGwdZGxGCRnTA10sOa0CWkLCLEcyLpRF4b3k9tXlsN2dLUNvnECgYBJKMMQP1zOR0OmBcF1VP3rS6w9Ffupbs0hNeIaBFBewGp4ltzwmJiJRNwMT3k9CVKXwqb+moyYWPpLnfXO9KdmvmPwJdREqP+BJzzqp0209R17Ygtp5taVfxKRegFehohtdd4tMCDBKUZ2/OPhaFr05+jN3ChQJmj4a9WU4KsvNwKBgEVkloN2HRAV7qazts20RwG38jltVf5yCiL4QwmCgaKIHntgoChYII8MEaHN7Zcu50mIYQOPmo/1EDo3koTvP/HKqfA1jTNN/9OC9UOYHU+e5p03xyjOHABWZyfWiuK70zcb0+WYhCaXVrlMqm2iFfwJGiDl2NMqbZdLSQyDR3VK";
        //支付宝公钥
        public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoQ4+vG8VMorL5D6AxXW8tGqYB8Ep7DcF9lWpC0qvPmxGlPptXOdHMsZnj86S8/Hyzht+P/Fg2RwZo9GlLpq3Ghw+1D/Y/n1h2YmNKyZ7Hrr9y8GUxwq/DFiYauoXYRL4w8EWyQauI74w/bew5n2Oppds/hPDhe6w9BhUjHlkvb8jQhiHgQpsxFKodtuBuX33ffj9197XllKU6Dre8t6olBRpTEvyGUegmXLjjHx/RAbuBDsn/SUxUyBqw+EiR37EBwTcMGzgLD8/BkEvTHFBOqueGA4A8gVnbVnrXLk9F6egkUYZUeTiEFHdqT7wZ8zqZExpZC9+7mDdvt8ZmGTQ5wIDAQAB";
        //签名方式
        public static String SIGN_TYPE = "RSA2";
        //字符编码格式
        public static String CHARSET="utf-8";
        //支付宝网关（沙箱环境）
        public static String GATEWAY_URL="https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        //支付宝异步通知路径
        public static String NOTIFY_URL = "";
        //支付宝同步通知路径
        public static String RETURN_URL = "http://localhost:5173/user";
    }

    public String pay(Long orderId, String seat, double amount) {
        String result = null;
        System.out.println(amount);
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.GATEWAY_URL, AlipayConfig.APP_ID, AlipayConfig.MERCHANT_PRIVATE_KEY, "json", AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.SIGN_TYPE);
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.RETURN_URL);
        //notify_url需要是外网地址
        //alipayRequest.setNotifyUrl(AlipayConfig.NOTIFY_URL);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId.toString());
        bizContent.put("total_amount", amount);
        bizContent.put("subject", seat);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        alipayRequest.setBizContent(bizContent.toString());
        try {
            AlipayTradePagePayResponse response = client.pageExecute(alipayRequest);
            result = response.getBody();
            System.out.println(result);
            if(response.isSuccess()){
                System.out.println("调用成功");
            }else{
                System.out.println("调用失败");
            }
        }catch(AlipayApiException e){
            e.printStackTrace();
        }

        return result;
    }

    public double calculateAmount(int price, int mileAgePoints) {
        double amount = price;
        //表驱动
        int[] pointsBound = {0, 1000, 3000, 10000, 50000, Integer.MAX_VALUE};
        double[] discount = {0, 0.001, 0.0015, 0.002, 0.0025, 0.003};

        for(int i = 0; i < 6; i++){
            if(mileAgePoints > pointsBound[i]){
                amount = amount * (1 - discount[i]);
            }
        }

        return amount;
    }

    public void refund(Long orderId, double amount){
        System.out.println(amount);
        //获得初始化的AlipayClient
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.GATEWAY_URL, AlipayConfig.APP_ID, AlipayConfig.MERCHANT_PRIVATE_KEY, "json", AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.SIGN_TYPE);
        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId.toString());
        bizContent.put("refund_amount", amount);

        alipayRequest.setBizContent(bizContent.toString());
        try {
            AlipayTradeRefundResponse response = client.execute(alipayRequest);
            if(response.isSuccess()){
                System.out.println("退款成功");
            }else{
                System.out.println("退款失败");
            }
        }catch(AlipayApiException e){
            e.printStackTrace();
        }
    }
}