package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.UserEntity;

public class MileagePointPaymentStrategy extends PaymentStrategy{
    //积分支付策略
    public static final MileagePointPaymentStrategy INSTANCE = new MileagePointPaymentStrategy();

    public String pay(Long orderId, String seat, double amount){
        //pay the order
        return null;
    }

    public double calculateAmount(int price, int mileAgePoints){
        //calculate the amount that user expected to pay
        //return the final amount expected to pay
        if (mileAgePoints < price * 10){
            throw new BizException(BizError.NOT_ENOUGH_MIEAGEPOINT);
        }
        return price * 10;
    }
}
