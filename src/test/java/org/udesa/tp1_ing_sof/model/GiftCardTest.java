package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GiftCardTest {

    private static final int VALID_INITIAL_BALANCE = 100;
    private static final String VALID_OWNER = "owner-42";

    @Test public void test01GiftCardStartsUnclaimedWithInitialBalance() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        assertEquals(VALID_INITIAL_BALANCE, card.getBalance());
    }

    @Test public void test02GiftCardCanNotBeCreatedWithZeroInitialBalance() {
        assertThrowsLike(() -> new GiftCard(0), GiftCard.InvalidInitialBalanceErrorDescription);
    }

    @Test public void test03GiftCardCanNotBeCreatedWithNegativeInitialBalance() {
        assertThrowsLike(() -> new GiftCard(-5), GiftCard.InvalidInitialBalanceErrorDescription);
    }

    @Test public void test04ClaimingSetsOwnerAndClaimed() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER);
        assertTrue(card.isClaimed());
        assertEquals(VALID_OWNER, card.getOwner());
    }

    @Test public void test05CanNotClaimAlreadyClaimedCard() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER);
        assertThrowsLike(() -> card.claimCard("other-owner"), GiftCard.AlreadyClaimedErrorDescription);
    }

    @Test public void test06IsClaimedReflectsState() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        card.claimCard(VALID_OWNER);
        assertTrue(card.isClaimed());
    }

    @Test public void test07GetOwnerNullBeforeClaim() {
        GiftCard card = newUnclaimedCard();
        assertNull(card.getOwner());
    }

    @Test public void test08IsOwnedBy() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER);
        assertTrue(card.isOwnedBy(VALID_OWNER));
        assertFalse(card.isOwnedBy("another"));
    }

    @Test public void test09GetBalanceOnUnclaimedCard() {
        GiftCard card = newUnclaimedCard();
        assertEquals(VALID_INITIAL_BALANCE, card.getBalance());
    }

    @Test public void test10ChargeReducesBalanceAndAddsTransaction() {
        GiftCard card = newClaimedCard();
        int before = card.getBalance();
        Clock clock = new Clock(LocalDateTime.of(2025, 1, 2, 3, 4));
        card.charge(30, VALID_OWNER, clock);
        assertEquals(before - 30, card.getBalance());
        assertEquals(1, card.getTransactions().size());
    }

    @Test public void test11MultipleChargesAccumulate() {
        GiftCard card = newClaimedCard();
        Clock clock1 = new Clock(LocalDateTime.of(2025, 1, 2, 3, 4));
        Clock clock2 = new Clock(LocalDateTime.of(2025, 2, 3, 4, 5));
        card.charge(30, VALID_OWNER, clock1);
        card.charge(20, VALID_OWNER, clock2);
        assertEquals(VALID_INITIAL_BALANCE - 50, card.getBalance());
        assertEquals(2, card.getTransactions().size());
    }

    @Test public void test12CanNotChargeIfNotClaimed() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.charge(10, VALID_OWNER, new Clock(LocalDateTime.now())), GiftCard.NotClaimedErrorDescription);
    }

    @Test public void test13CanNotChargeIfNotOwner() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(10, "wrong-user", new Clock(LocalDateTime.now())), GiftCard.NotOwnerErrorDescription);
    }

    @Test public void test14CanNotChargeZeroAmount() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(0, VALID_OWNER, new Clock(LocalDateTime.now())), GiftCard.InvalidAmountErrorDescription);
    }

    @Test public void test15CanNotChargeNegativeAmount() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(-3, VALID_OWNER, new Clock(LocalDateTime.now())), GiftCard.InvalidAmountErrorDescription);
    }

    @Test public void test16CanNotChargeMoreThanBalance() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(VALID_INITIAL_BALANCE + 1, VALID_OWNER, new Clock(LocalDateTime.now())), GiftCard.InsufficientBalanceErrorDescription);
    }

    @Test public void test17GetTransactionsOnUnclaimedCard() {
        GiftCard card = newUnclaimedCard();
        assertTrue(card.getTransactions().isEmpty());
    }

    @Test public void test18TransactionsHaveOrderOfAddition() {
        GiftCard card = newClaimedCard();
        Clock clock1 = new Clock(LocalDateTime.of(2025, 1, 2, 3, 4));
        Clock clock2 = new Clock(LocalDateTime.of(2025, 1, 2, 3, 5));
        card.charge(10, VALID_OWNER, clock1);
        card.charge(5, VALID_OWNER, clock2);
        assertEquals(2, card.getTransactions().size());
    }

    @Test public void test19TransactionHasCorrectAmountAndTime() {
        GiftCard card = newClaimedCard();
        LocalDateTime chargeTime = LocalDateTime.of(2025, 1, 2, 3, 4);
        Clock clock = new Clock(chargeTime);
        card.charge(25, VALID_OWNER, clock);
        
        Transaction transaction = card.getTransactions().get(0);
        assertEquals(25, transaction.getAmount());
        assertEquals(chargeTime, transaction.getTime());
    }

    @Test public void test20TransactionsAreImmutable() {
        GiftCard card = newClaimedCard();
        card.charge(15, VALID_OWNER, new Clock(LocalDateTime.now()));
        
        Transaction transaction = card.getTransactions().get(0);
        assertEquals(15, transaction.getAmount());
    }

    @Test public void test21MultipleTransactionsHaveCorrectDetails() {
        GiftCard card = newClaimedCard();
        LocalDateTime time1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2025, 1, 2, 11, 0);
        Clock clock1 = new Clock(time1);
        Clock clock2 = new Clock(time2);
        
        card.charge(20, VALID_OWNER, clock1);
        card.charge(30, VALID_OWNER, clock2);
        
        assertEquals(20, card.getTransactions().get(0).getAmount());
        assertEquals(time1, card.getTransactions().get(0).getTime());
        assertEquals(30, card.getTransactions().get(1).getAmount());
        assertEquals(time2, card.getTransactions().get(1).getTime());
    }

    @Test public void test22TransactionsListIsUnmodifiable() {
        GiftCard card = newClaimedCard();
        card.charge(5, VALID_OWNER, new Clock(LocalDateTime.now()));
        assertThrows(UnsupportedOperationException.class, () -> card.getTransactions().add(null));
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }

    private GiftCard newUnclaimedCard() { return new GiftCard(VALID_INITIAL_BALANCE); }
    private GiftCard newClaimedCard() { GiftCard c = newUnclaimedCard(); c.claimCard(VALID_OWNER); return c; }
}