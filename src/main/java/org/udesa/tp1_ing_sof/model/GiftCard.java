package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    int cardId;
    int ownerId;
    boolean claimed =  false;
    int balance;
    List<Transaction> transactions = new ArrayList<Transaction>();

    public GiftCard(int balance, int cardId) {
        this.balance = balance;
        this.cardId = cardId;
    }

    public void claimCard(int ownerId){
        this.ownerId = ownerId;
        this.claimed = true;
    }

    public void charge(int amount, LocalDateTime time){
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        this.balance -= amount;
        transactions.add(new Transaction(amount, time));
    }

}
