package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GiftCardTest {

    public static final LocalDateTime VALID_CHARGE_TIME = LocalDateTime.of(2025, 1, 2, 3, 4);
    private static final int VALID_INITIAL_BALANCE = 100;
    private static final String VALID_OWNER = "owner-42";
    private static final String VALID_MERCHANT_KEY = "MercadoPagoKey";
    private static final String VALID_MERCHANT_KEY_2 = "ModoKey";

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
        assertEquals(VALID_OWNER, card.getOwner());
        assertEquals(VALID_INITIAL_BALANCE, card.getBalance(VALID_OWNER));
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
        assertThrowsLike(() -> card.charge(10, VALID_OWNER, VALID_MERCHANT_KEY, new Clock(LocalDateTime.now())), GiftCard.NotClaimedErrorDescription);
    }

    @Test public void test12CanNotChargeIfNotOwner() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(10, "wrong-user", VALID_MERCHANT_KEY, new Clock(LocalDateTime.now())), GiftCard.NotOwnerErrorDescription);
    }

    @Test public void test13CanNotChargeZeroAmount() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(0, VALID_OWNER, VALID_MERCHANT_KEY, new Clock(LocalDateTime.now())), GiftCard.InvalidAmountErrorDescription);
    }

    @Test public void test14CanNotChargeNegativeAmount() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(-3, VALID_OWNER, VALID_MERCHANT_KEY, new Clock(LocalDateTime.now())), GiftCard.InvalidAmountErrorDescription);
    }

    @Test public void test15CanNotChargeMoreThanBalance() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.charge(VALID_INITIAL_BALANCE + 1, VALID_OWNER, VALID_MERCHANT_KEY, new Clock(LocalDateTime.now())), GiftCard.InsufficientBalanceErrorDescription);
    }

    @Test public void test16ChargeReducesBalanceAndAddsTransaction() {
        GiftCard card = newClaimedCard();
        int before = card.getBalance(VALID_OWNER);
        Clock clock = new Clock(VALID_CHARGE_TIME);
        card.charge(30, VALID_MERCHANT_KEY, VALID_OWNER, clock);
        assertEquals(before - 30, card.getBalance(VALID_OWNER));
        assertEquals(1, card.getTransactions(VALID_OWNER).size());
    }

    @Test public void test17MultipleChargesAccumulate() {
        GiftCard card = newClaimedCard();
        card.charge(30, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));
        card.charge(20, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME.plusDays(1)));
        assertEquals(VALID_INITIAL_BALANCE - 50, card.getBalance(VALID_OWNER));
        assertEquals(2, card.getTransactions(VALID_OWNER).size());
    }

    @Test public void test18CantGetTransactionsOnUnclaimedCard() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.getTransactions(VALID_OWNER), GiftCard.CantGetTransactionsOfUnclaimedCardErrorDescription);
    }

    @Test public void test19CantGetTransactionsOfInvalidOwner() {
        GiftCard card = newClaimedCard();
        assertThrowsLike(() -> card.getTransactions("wrong user"), GiftCard.NotOwnerErrorDescription);
    }

    @Test public void test20TransactionHasCorrectTime() {
        GiftCard card = newClaimedCard();
        card.charge(25, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));

        Transaction transaction = card.getTransactions(VALID_OWNER).get(0);
        assertEquals(VALID_CHARGE_TIME, transaction.getTime());
    }

    @Test public void test21TransactionHasCorrectAmount() {
        GiftCard card = newClaimedCard();
        card.charge(25, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));

        Transaction transaction = card.getTransactions(VALID_OWNER).get(0);
        assertEquals(25, transaction.getAmount());
    }

    @Test public void test22TransactionHasCorrectMerchantKey() {
        GiftCard card = newClaimedCard();
        card.charge(25, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));

        Transaction transaction = card.getTransactions(VALID_OWNER).get(0);
        assertEquals(VALID_MERCHANT_KEY, transaction.getMerchantKey());
    }

    @Test public void test23TransactionsHaveOrderOfAddition() {
        GiftCard card = newClaimedCard();
        card.charge(10, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(LocalDateTime.now()));
        card.charge(5, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(LocalDateTime.now()));
        assertEquals(2, card.getTransactions(VALID_OWNER).size());
        assertEquals(10, card.getTransactions(VALID_OWNER).get(0).getAmount());
        assertEquals(5, card.getTransactions(VALID_OWNER).get(1).getAmount());
    }

    @Test public void test24TransactionsAreImmutable() {
        GiftCard card = newClaimedCard();
        card.charge(15, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(LocalDateTime.now()));

        assertEquals(15, card.getTransactions(VALID_OWNER).get(0).getAmount());
    }

    @Test public void test25MultipleTransactionsHaveAmount() {
        GiftCard card = newClaimedCard();

        card.charge(20, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));
        card.charge(30, VALID_MERCHANT_KEY_2, VALID_OWNER, new Clock(VALID_CHARGE_TIME.plusMinutes(1)));

        assertEquals(20, card.getTransactions(VALID_OWNER).get(0).getAmount());
        assertEquals(30, card.getTransactions(VALID_OWNER).get(1).getAmount());
    }

    @Test public void test26MultipleTransactionsHaveTime() {
        GiftCard card = newClaimedCard();

        card.charge(20, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));
        card.charge(30, VALID_MERCHANT_KEY_2, VALID_OWNER, new Clock(VALID_CHARGE_TIME.plusMinutes(1)));

        assertEquals(VALID_CHARGE_TIME, card.getTransactions(VALID_OWNER).get(0).getTime());
        assertEquals(VALID_CHARGE_TIME.plusMinutes(1), card.getTransactions(VALID_OWNER).get(1).getTime());
    }

    @Test public void test27MultipleTransactionsHaveMerchantKey() {
        GiftCard card = newClaimedCard();

        card.charge(20, VALID_MERCHANT_KEY, VALID_OWNER, new Clock(VALID_CHARGE_TIME));
        card.charge(30, VALID_MERCHANT_KEY_2, VALID_OWNER, new Clock(VALID_CHARGE_TIME.plusMinutes(1)));

        assertEquals(VALID_MERCHANT_KEY,  card.getTransactions(VALID_OWNER).get(0).getMerchantKey());
        assertEquals(VALID_MERCHANT_KEY_2,  card.getTransactions(VALID_OWNER).get(1).getMerchantKey());
    }


    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }

    private GiftCard newUnclaimedCard() { return new GiftCard(VALID_INITIAL_BALANCE); }
    private GiftCard newClaimedCard() { GiftCard c = newUnclaimedCard(); c.claimCard(VALID_OWNER); return c; }
}
