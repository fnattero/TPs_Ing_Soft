package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GiftCardTest {

    public static final LocalDateTime BaseChargeTime = LocalDateTime.of(2025, 1, 2, 3, 4);
    private static final int InitialBalance = 100;
    private static final String ValidOwner = "OwnerJuan";
    private static final String ValidMerchantKey = "MercadoPagoKey";
    private static final String ValidMerchantKey2 = "ModoKey";

    @Test public void test01GiftCardCanNotBeCreatedWithZeroInitialBalance() {
        assertThrowsLike(() -> new GiftCard(0), GiftCard.InvalidInitialBalanceErrorDescription);
    }

    @Test public void test02GiftCardCanNotBeCreatedWithNegativeInitialBalance() {
        assertThrowsLike(() -> new GiftCard(-5), GiftCard.InvalidInitialBalanceErrorDescription);
    }

    @Test public void test03NewGiftCardStartsUnclaimedWithoutOwner() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        assertNull(card.getOwner());
    }

    @Test public void test04ClaimingSetsOwnerAndClaimed() {
        GiftCard card = newClaimedCard();
        assertTrue(card.isClaimed());
        assertEquals(ValidOwner, card.getOwner());
        assertEquals(InitialBalance, card.getBalance(ValidOwner));
    }

    @Test public void test05CanNotClaimAlreadyClaimedCard() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(ValidOwner);
        assertThrowsLike(() -> card.claimCard("other-owner"), GiftCard.AlreadyClaimedErrorDescription);
    }

    @Test public void test06IsClaimedReflectsState() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        card.claimCard(ValidOwner);
        assertTrue(card.isClaimed());
    }

    @Test public void test07GetOwnerNullBeforeClaim() {
        GiftCard card = newUnclaimedCard();
        assertNull(card.getOwner());
    }

    @Test public void test08IsOwnedBy() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(ValidOwner);
        assertTrue(card.isOwnedBy(ValidOwner));
        assertFalse(card.isOwnedBy("another"));
    }

    @Test public void test09UnclaimedCardCanNotExposeBalance() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.getBalance("any-user"), GiftCard.CantGetBalanceOfUnclaimedCardErrorDescription);
    }

    @Test public void test10ClaimedCardRejectsBalanceRequestsFromNonOwner() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.getBalance("wrong-user"), GiftCard.NotOwnerErrorDescription);
    }

    @Test public void test11CanNotChargeIfNotClaimed() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.charge(10, ValidOwner, ValidMerchantKey, new Clock()), GiftCard.CantChargeUnclaimedCardErrorDescription);
    }

    @Test public void test12CanNotChargeIfNotOwner() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(10, "wrong-user", ValidMerchantKey, new Clock()), GiftCard.NotOwnerErrorDescription);
    }

    @Test public void test13CanNotChargeZeroAmount() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(0, ValidOwner, ValidMerchantKey, new Clock()), GiftCard.InvalidAmountErrorDescription);
    }

    @Test public void test14CanNotChargeNegativeAmount() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(-3, ValidOwner, ValidMerchantKey, new Clock()), GiftCard.InvalidAmountErrorDescription);
    }

    @Test public void test15CanNotChargeMoreThanBalance() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(InitialBalance + 1, ValidOwner, ValidMerchantKey, new Clock()), GiftCard.InsufficientBalanceErrorDescription);
    }

    @Test public void test16ChargeReducesBalanceAndAddsTransaction() {
        GiftCard card = newClaimedCard();
        int before = card.getBalance(ValidOwner);
        card.charge(30, ValidMerchantKey, ValidOwner, new Clock());
        assertEquals(before - 30, card.getBalance(ValidOwner));
        assertEquals(1, card.getTransactions(ValidOwner).size());
    }

    @Test public void test17MultipleChargesAccumulate() {
        GiftCard card = newClaimedCard();
        card.charge(30, ValidMerchantKey, ValidOwner, new Clock());
        card.charge(20, ValidMerchantKey, ValidOwner, new Clock());
        assertEquals(InitialBalance - 50, card.getBalance(ValidOwner));
        assertEquals(2, card.getTransactions(ValidOwner).size());
    }

    @Test public void test18CantGetTransactionsOnUnclaimedCard() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.getTransactions(ValidOwner), GiftCard.CantGetTransactionsOfUnclaimedCardErrorDescription);
    }

    @Test public void test19CantGetTransactionsOfInvalidOwner() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.getTransactions("wrong user"), GiftCard.NotOwnerErrorDescription);
    }

    @Test public void test20TransactionHasCorrectTime() {
        GiftCard card = newClaimedCard();
        Clock clock = seqClock(BaseChargeTime, BaseChargeTime);
        card.charge(25, ValidMerchantKey, ValidOwner, clock);

        Transaction transaction = card.getTransactions(ValidOwner).get(0);
        assertEquals(BaseChargeTime, transaction.getTime());
    }

    @Test public void test21TransactionHasCorrectAmount() {
        GiftCard card = newClaimedCard();
        card.charge(25, ValidMerchantKey, ValidOwner, new Clock());

        Transaction transaction = card.getTransactions(ValidOwner).get(0);
        assertEquals(25, transaction.getAmount());
    }

    @Test public void test22TransactionHasCorrectMerchantKey() {
        GiftCard card = newClaimedCard();
        card.charge(25, ValidMerchantKey, ValidOwner, new Clock());

        Transaction transaction = card.getTransactions(ValidOwner).get(0);
        assertEquals(ValidMerchantKey, transaction.getMerchantKey());
    }

    @Test public void test23TransactionsHaveOrderOfAddition() {
        GiftCard card = newClaimedCard();
        card.charge(10, ValidMerchantKey, ValidOwner, new Clock());
        card.charge(5, ValidMerchantKey, ValidOwner, new Clock());
        assertEquals(2, card.getTransactions(ValidOwner).size());
        assertEquals(10, card.getTransactions(ValidOwner).get(0).getAmount());
        assertEquals(5, card.getTransactions(ValidOwner).get(1).getAmount());
    }

    @Test public void test24MultipleTransactionsInDifferentTimesHaveCorrectAmount() {
        GiftCard card = newClaimedCard();

        Clock clock = seqClock(BaseChargeTime, BaseChargeTime.plusMinutes(1));

        card.charge(20, ValidMerchantKey, ValidOwner, clock);
        card.charge(30, ValidMerchantKey2, ValidOwner, clock);

        assertEquals(20, card.getTransactions(ValidOwner).get(0).getAmount());
        assertEquals(30, card.getTransactions(ValidOwner).get(1).getAmount());
    }

    @Test public void test25MultipleTransactionsInDifferentTimesHaveCorrectTime() {
        GiftCard card = newClaimedCard();

        Clock clock = seqClock(BaseChargeTime, BaseChargeTime.plusMinutes(1));

        card.charge(20, ValidMerchantKey, ValidOwner, clock);
        card.charge(30, ValidMerchantKey2, ValidOwner, clock);

        assertEquals(BaseChargeTime, card.getTransactions(ValidOwner).get(0).getTime());
        assertEquals(BaseChargeTime.plusMinutes(1), card.getTransactions(ValidOwner).get(1).getTime());
    }

    @Test public void test26MultipleTransactionsInDifferentTimesHaveCorrectMerchantKey() {
        GiftCard card = newClaimedCard();

        Clock clock = seqClock(BaseChargeTime, BaseChargeTime.plusMinutes(1));

        card.charge(20, ValidMerchantKey, ValidOwner, clock);
        card.charge(30, ValidMerchantKey2, ValidOwner, clock);

        assertEquals(ValidMerchantKey,  card.getTransactions(ValidOwner).get(0).getMerchantKey());
        assertEquals(ValidMerchantKey2,  card.getTransactions(ValidOwner).get(1).getMerchantKey());
    }


    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }

    private GiftCard newUnclaimedCard() { return new GiftCard(InitialBalance); }
    private GiftCard newClaimedCard() { GiftCard c = newUnclaimedCard(); c.claimCard(ValidOwner); return c; }

    private Clock seqClock(LocalDateTime... times) {
        return new Clock() {
            Iterator<LocalDateTime> seq = java.util.List.of(times).iterator();
            public LocalDateTime getTime() { return seq.next(); }
        };
    }
}
