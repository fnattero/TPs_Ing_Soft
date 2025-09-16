package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String AlreadyClaimedErrorDescription = "Already Claimed";
    public static final String InvalidAmountErrorDescription = "Invalid amount";

    int cardId;
    int ownerId;
    int balance;
    List<Transaction> transactions = new ArrayList<Transaction>();

    private GiftCardState state;

    public GiftCard(int balance, int cardId) {
        this.balance = balance;
        this.cardId = cardId;
        this.state = new UnclaimedState();
    }

    public void claimCard(int ownerId) {
        state.claimCard(this, ownerId);
    }

    public void charge(int amount, LocalDateTime time) {
        if (amount <= 0) {
            throw new RuntimeException(InvalidAmountErrorDescription);
        }
        this.balance -= amount;
        transactions.add(new Transaction(amount, time));
    }

    public boolean isClaimed() {
        return state.isClaimed();
    }

    private static abstract class GiftCardState {
        abstract void claimCard(GiftCard card, int ownerId);
        abstract boolean isClaimed();
    }

    private static class UnclaimedState extends GiftCardState {
        @Override
        public void claimCard(GiftCard card, int ownerId) {
            card.ownerId = ownerId;
            card.state = new ClaimedState();
        }

        @Override
        public boolean isClaimed() {
            return false;
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
    }
}
