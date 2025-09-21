package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;

public class Transaction {
    public static final String negativeAmountErrorDescription = "amount must be non-negative";
    public static final String futureTimeErrorDescription = "time cannot be in the future";

    private final int amount;
    private final LocalDateTime time;
    private final String merchantKey;

    public Transaction(int amount, String merchantKey, LocalDateTime time){
        this.amount = amount;
        this.time = time;
        this.merchantKey = merchantKey;
    }

    public int getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }
    public String getMerchantKey() { return merchantKey; }
}
