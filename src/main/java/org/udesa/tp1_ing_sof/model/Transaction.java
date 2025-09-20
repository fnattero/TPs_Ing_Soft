package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;

public class Transaction {
    public static final String negativeAmountErrorDescription = "amount must be non-negative";
    public static final String futureTimeErrorDescription = "time cannot be in the future";

    private final int amount;
    private final LocalDateTime time;
    private final String merchantKey;

    public Transaction(int amount, String merchantKey, LocalDateTime time){
        validate(amount, time);
        this.amount = amount;
        this.time = time;
        this.merchantKey = merchantKey;
    }

    private static void validate(int amount, LocalDateTime time) {
        validateAmount(amount);
        validateTimeNotFuture(time);
    }

    private static void validateAmount(int amount) {if (amount < 0) throw new IllegalArgumentException(negativeAmountErrorDescription);}
    private static void validateTimeNotFuture(LocalDateTime time) { if (time != null && time.isAfter(LocalDateTime.now())) throw new IllegalArgumentException(futureTimeErrorDescription);}

    public int getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }
    public String getMerchantKey() { return merchantKey; }
}
