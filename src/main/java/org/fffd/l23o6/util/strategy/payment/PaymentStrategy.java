package org.fffd.l23o6.util.strategy.payment;

public abstract class PaymentStrategy {

    // : implement this by adding necessary methods and implement specified strategy
    public String pay(Long orderId, String seat, double amount){
        //pay the order
        return null;
    }

    public double calculateAmount(int price, int mileAgePoints){
        //calculate the amount that user expected to pay
        //return the final amount expected to pay
        return 0;
    }
}
