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

    String owner;
    int balance;
    List<Transaction> transactions = new ArrayList<Transaction>();
    private GiftCardState state;

    public GiftCard(int balance) {
        validateInitialBalance(balance);
        this.balance = balance;
        this.state = new UnclaimedState();
    }

    public void claimCard(String owner) { state.claimCard(this, owner); }
    public void charge(int amount, LocalDateTime time) { state.charge(this, amount, time); }
    public boolean isClaimed() { return state.isClaimed(); }
    public int getBalance() { return balance; }
    public String getOwner() { return state.getOwner(this); }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public boolean isOwnedBy(String user) { return user != null && user.equals(getOwner()); }

    private static abstract class GiftCardState {
        abstract void claimCard(GiftCard card, String owner);
        abstract boolean isClaimed();
        abstract void charge(GiftCard card, int amount, LocalDateTime time);
        abstract String getOwner(GiftCard card);
    }

    private static class UnclaimedState extends GiftCardState {
        @Override void claimCard(GiftCard card, String owner) {
            validateClaimOwner(owner);
            card.owner = owner;
            card.state = new ClaimedState();
        }
        @Override boolean isClaimed() { return false; }
        @Override void charge(GiftCard card, int amount, LocalDateTime time) { throw new RuntimeException(NotClaimedErrorDescription); }
        @Override String getOwner(GiftCard card) { return null; }
    }

    private static class ClaimedState extends GiftCardState {
        @Override void claimCard(GiftCard card, String owner) { throw new RuntimeException(AlreadyClaimedErrorDescription); }
        @Override boolean isClaimed() { return true; }
        @Override void charge(GiftCard card, int amount, LocalDateTime time) {
            validateCharge(card, amount);
            card.balance -= amount;
            card.transactions.add(new Transaction(amount, time));
        }
        @Override String getOwner(GiftCard card) { return card.owner; }
    }

    private static void validateInitialBalance(int balance) { checkInitialBalancePositive(balance); }
    private static void checkInitialBalancePositive(int balance) { if (balance <= 0) throw new RuntimeException(InvalidInitialBalanceErrorDescription); }
    private static void validateClaimOwner(String owner) { checkOwnerNotBlank(owner); }
    private static void checkOwnerNotBlank(String owner) { if (owner == null || owner.isBlank()) throw new RuntimeException(InvalidOwnerIdErrorDescription); }
    private static void validateCharge(GiftCard card, int amount) { checkAmountPositive(amount); checkSufficientBalance(card, amount); }
    private static void checkAmountPositive(int amount) { if (amount <= 0) throw new RuntimeException(InvalidAmountErrorDescription); }
    private static void checkSufficientBalance(GiftCard card, int amount) { if (amount > card.balance) throw new RuntimeException(InsufficientBalanceErrorDescription); }
}
