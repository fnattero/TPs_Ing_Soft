package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class GiftCard {
    public static final String AlreadyClaimedErrorDescription = "Already Claimed";
    public static final String InvalidAmountErrorDescription = "Invalid amount";
    public static final String NotClaimedErrorDescription = "Card not claimed";
    public static final String InvalidInitialBalanceErrorDescription = "Invalid initial balance";
    public static final String InvalidOwnerIdErrorDescription = "Invalid owner id";
    public static final String InsufficientBalanceErrorDescription = "Insufficient balance";

    int cardId;
    int ownerId;
    int balance;
    List<Transaction> transactions = new ArrayList<Transaction>();

    private GiftCardState state;

    public GiftCard(int balance, int cardId) {
        if (balance <= 0) throw new RuntimeException(InvalidInitialBalanceErrorDescription);
        this.balance = balance;
        this.cardId = cardId;
        this.state = new UnclaimedState();
    }

    public void claimCard(int ownerId) {
        state.claimCard(this, ownerId);
    }

    public void charge(int amount, LocalDateTime time) {
        state.charge(this, amount, time);
    }

    public boolean isClaimed() {
        return state.isClaimed();
    }

    public int getBalance() {
        return balance;
    }

    public int getCardId() {
        return cardId;
    }

    public Integer getOwnerId() {
        return state.getOwnerId(this);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public boolean isOwnedBy(int userId) {
        return Integer.valueOf(userId).equals(getOwnerId());
    }

    private static abstract class GiftCardState {
        abstract void claimCard(GiftCard card, int ownerId);
        abstract boolean isClaimed();
        abstract void charge(GiftCard card, int amount, LocalDateTime time);
        abstract Integer getOwnerId(GiftCard card);
    }

    private static class UnclaimedState extends GiftCardState {
        @Override
        public void claimCard(GiftCard card, int ownerId) {
            if (ownerId <= 0) throw new RuntimeException(InvalidOwnerIdErrorDescription);
            card.ownerId = ownerId;
            card.state = new ClaimedState();
        }

        @Override
        public boolean isClaimed() {
            return false;
        }

        @Override
        public void charge(GiftCard card, int amount, LocalDateTime time) {
            throw new RuntimeException(NotClaimedErrorDescription);
        }

        @Override
        public Integer getOwnerId(GiftCard card) {
            return null;
        }
    }

    private static class ClaimedState extends GiftCardState {
        @Override
        public void claimCard(GiftCard card, int ownerId) {
            throw new RuntimeException(AlreadyClaimedErrorDescription);
        }

        @Override
        public boolean isClaimed() {
            return true;
        }

        @Override
        public void charge(GiftCard card, int amount, LocalDateTime time) {
            ensureValidCharge(card, amount);
            card.balance -= amount;
            card.transactions.add(new Transaction(amount, time));
        }
        //s

        private void ensureValidCharge(GiftCard card, int amount) {
            if (amount <= 0) throw new RuntimeException(InvalidAmountErrorDescription);
            if (amount > card.balance) throw new RuntimeException(InsufficientBalanceErrorDescription);
        }

        @Override
        public Integer getOwnerId(GiftCard card) {
            return card.ownerId;
        }
    }
}
