package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;

public class Transaction {
    private static final String ERROR_AMOUNT_NEGATIVE = "amount must be non-negative";
    private static final String ERROR_TIME_NULL = "time cannot be null";
    private static final String ERROR_TIME_FUTURE = "time cannot be in the future";

    private final int amount;
    private final LocalDateTime time;

    public Transaction(int amount, LocalDateTime time){
        validate(amount, time);
        this.amount = amount;
        this.time = time;
    }

    private static void validate(int amount, LocalDateTime time) {
        validateAmount(amount);
        validateTimeNotNull(time);
        validateTimeNotFuture(time);
    }

    private static void validateAmount(int amount) {if (amount < 0) throw new IllegalArgumentException(ERROR_AMOUNT_NEGATIVE);}
    private static void validateTimeNotNull(LocalDateTime time) {if (time == null) throw new IllegalArgumentException(ERROR_TIME_NULL);}
    private static void validateTimeNotFuture(LocalDateTime time) { if (time != null && time.isAfter(LocalDateTime.now())) throw new IllegalArgumentException(ERROR_TIME_FUTURE);}

    public int getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }
}
