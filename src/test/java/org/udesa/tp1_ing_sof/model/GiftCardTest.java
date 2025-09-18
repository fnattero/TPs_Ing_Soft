package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GiftCardTest {

    private static final int VALID_INITIAL_BALANCE = 100;
    private static final int VALID_OWNER_ID = 42;

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }

    private GiftCard newUnclaimedCard() { return new GiftCard(VALID_INITIAL_BALANCE, 1); }
    private GiftCard newClaimedCard() { GiftCard c = newUnclaimedCard(); c.claimCard(VALID_OWNER_ID); return c; }

    @Test public void test01GiftCardStartsUnclaimedWithInitialBalance() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        assertEquals(VALID_INITIAL_BALANCE, card.balance);
    }

    @Test public void test02GiftCardCanNotBeCreatedWithZeroOrNegativeInitialBalance() {
        assertThrowsLike(() -> new GiftCard(0, 1), GiftCard.InvalidInitialBalanceErrorDescription);
        assertThrowsLike(() -> new GiftCard(-5, 1), GiftCard.InvalidInitialBalanceErrorDescription);
    }

    @Test public void test03ClaimingSetsOwnerAndClaimed() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER_ID);
        assertTrue(card.isClaimed());
        assertEquals(VALID_OWNER_ID, card.ownerId);
    }

    @Test public void test04CanNotClaimWithInvalidOwnerId() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.claimCard(-10), GiftCard.InvalidOwnerIdErrorDescription);
    }

    @Test public void test05CanNotClaimAlreadyClaimedCard() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER_ID);
        assertThrowsLike(() -> card.claimCard(99), GiftCard.AlreadyClaimedErrorDescription);
    }

    @Test public void test06IsClaimedReflectsState() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        card.claimCard(VALID_OWNER_ID);
        assertTrue(card.isClaimed());
    }

    @Test public void test07ChargeReducesBalanceAndAddsTransaction() {
        GiftCard card = newClaimedCard();
        int before = card.balance;
        card.charge(30, LocalDateTime.of(2025, 1, 2, 3, 4));
        assertEquals(before - 30, card.balance);
        assertEquals(1, card.transactions.size());
    }

    @Test public void test08MultipleChargesAccumulate() {
        GiftCard card = newClaimedCard();
        card.charge(30, LocalDateTime.of(2025, 1, 2, 3, 4));
        card.charge(20, LocalDateTime.of(2025, 2, 3, 4, 5));
        assertEquals(VALID_INITIAL_BALANCE - 50, card.balance);
        assertEquals(2, card.transactions.size());
    }

    @Test public void test09CanNotChargeNonPositiveAmount() {
        GiftCard card = newClaimedCard();
        for (int invalid : new int[]{0, -3}) {
            int amount = invalid;
            assertThrowsLike(() -> card.charge(amount, LocalDateTime.now()), GiftCard.InvalidAmountErrorDescription);
        }
    }

    @Test public void test10CanNotChargeMoreThanBalance() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(VALID_INITIAL_BALANCE + 1, LocalDateTime.now()), GiftCard.InsufficientBalanceErrorDescription);
    }

    @Test public void test11CanNotChargeIfNotClaimed() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.charge(10, LocalDateTime.now()), GiftCard.NotClaimedErrorDescription);
    }

    @Test public void test12TransactionsHaveOrderOfAddition() {
        GiftCard card = newClaimedCard();
        LocalDateTime t1 = LocalDateTime.of(2025, 1, 2, 3, 4);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 2, 3, 5);
        card.charge(10, t1);
        card.charge(5, t2);
        assertEquals(2, card.transactions.size());
    }
}
