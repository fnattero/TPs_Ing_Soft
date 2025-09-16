package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;

public class Transaction {
    private int amount;
    private LocalDateTime time;
    public Transaction(int amount, LocalDateTime time){
        this.amount = amount;
        this.time = time;
    }
}
