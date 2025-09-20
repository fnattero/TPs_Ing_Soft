package org.udesa.tp1_ing_sof.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class GiftCard {
    public static final String AlreadyClaimedErrorDescription = "Already Claimed";
    public static final String InvalidAmountErrorDescription = "Invalid amount";
    public static final String NotClaimedErrorDescription = "Card not claimed";
    public static final String InvalidInitialBalanceErrorDescription = "Invalid initial balance";
    public static final String InsufficientBalanceErrorDescription = "Insufficient balance";
    public static final String NotOwnerErrorDescription = "User is not the owner";
    public static final String CantGetBalanceOfUnclaimedCardErrorDescription = "Can't get balance of unclaimed card";
    public static final String InvalidOwnerNameDescription = "Invalid owner name";

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
    public void charge(int amount, String user, Clock clock) { state.charge(this, amount, user, clock); }
    public boolean isClaimed() { return state.isClaimed(); }
    public int getBalance(String user) { return state.getBalance(this, user); }
    public String getOwner() { return state.getOwner(this); }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public boolean isOwnedBy(String user) { return user != null && user.equals(getOwner()); }

    private static abstract class GiftCardState {
        abstract void claimCard(GiftCard card, String owner);
        abstract boolean isClaimed();
        abstract void charge(GiftCard card, int amount, String user, Clock clock);
        abstract String getOwner(GiftCard card);
        abstract int getBalance(GiftCard card, String user);
    }

    private static class UnclaimedState extends GiftCardState {
        void claimCard(GiftCard card, String owner) {
            validateClaimOwner(owner);
            card.owner = owner;
            card.state = new ClaimedState();
        }
        boolean isClaimed() { return false; }
        void charge(GiftCard card, int amount, String user, Clock clock) { throw new RuntimeException(NotClaimedErrorDescription); }
        int getBalance(GiftCard card, String user) { throw new RuntimeException(CantGetBalanceOfUnclaimedCardErrorDescription); }
        String getOwner(GiftCard card) { return null; }
    }

    private static class ClaimedState extends GiftCardState {
        void claimCard(GiftCard card, String owner) { throw new RuntimeException(AlreadyClaimedErrorDescription); }
        boolean isClaimed() { return true; }
        void charge(GiftCard card, int amount, String user, Clock clock) {
            validateCharge(card, amount);
            validateOwnership(card, user);
            card.balance -= amount;
            card.transactions.add(new Transaction(amount, clock.getTime()));
        }
        int getBalance(GiftCard card, String user) {
            validateOwnership(card, user);
            return card.balance;
        }
        String getOwner(GiftCard card) { return card.owner; }
    }

    private static void validateInitialBalance(int balance) { checkInitialBalancePositive(balance); }
    private static void checkInitialBalancePositive(int balance) { if (balance <= 0) throw new RuntimeException(InvalidInitialBalanceErrorDescription); }
    private static void validateClaimOwner(String owner) { checkOwnerNotBlank(owner); }
    private static void checkOwnerNotBlank(String owner) { if (owner == null || owner.isBlank()) throw new RuntimeException(InvalidOwnerNameDescription); }
    private static void validateCharge(GiftCard card, int amount) { checkAmountPositive(amount); checkSufficientBalance(card, amount); }
    private static void validateOwnership(GiftCard card, String user) { if (!card.isOwnedBy(user)) throw new RuntimeException(NotOwnerErrorDescription); }
    private static void checkAmountPositive(int amount) { if (amount <= 0) throw new RuntimeException(InvalidAmountErrorDescription); }
    private static void checkSufficientBalance(GiftCard card, int amount) { if (amount > card.balance) throw new RuntimeException(InsufficientBalanceErrorDescription); }
}
