package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GiftCardTest {

    private static final int VALID_INITIAL_BALANCE = 100;
    private static final String VALID_OWNER = "owner-42";

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }

    private GiftCard newUnclaimedCard() { return new GiftCard(VALID_INITIAL_BALANCE); }
    private GiftCard newClaimedCard() { GiftCard c = newUnclaimedCard(); c.claimCard(VALID_OWNER); return c; }

    @Test public void test01GiftCardStartsUnclaimedWithInitialBalance() {
        GiftCard card = newUnclaimedCard();
        assertFalse(card.isClaimed());
        assertEquals(VALID_INITIAL_BALANCE, card.getBalance());
    }

    @Test public void test02GiftCardCanNotBeCreatedWithZeroOrNegativeInitialBalance() {
        assertThrowsLike(() -> new GiftCard(0), GiftCard.InvalidInitialBalanceErrorDescription);
        assertThrowsLike(() -> new GiftCard(-5), GiftCard.InvalidInitialBalanceErrorDescription);
    } //separar en dos

    @Test public void test03ClaimingSetsOwnerAndClaimed() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER);
        assertTrue(card.isClaimed());
        assertEquals(VALID_OWNER, card.getOwner());
    }

    @Test public void test04CanNotClaimWithInvalidOwner() {
        GiftCard card = newUnclaimedCard();
        assertThrowsLike(() -> card.claimCard(""), GiftCard.InvalidOwnerIdErrorDescription);
    } // revisar, esto es algo de facade, no deberiamos chequear nada de username en giftcard

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

    @Test public void test07ChargeReducesBalanceAndAddsTransaction() {
        GiftCard card = newClaimedCard();
        int before = card.getBalance();
        card.charge(30, LocalDateTime.of(2025, 1, 2, 3, 4));
        assertEquals(before - 30, card.getBalance());
        assertEquals(1, card.getTransactions().size());
    }

    @Test public void test08MultipleChargesAccumulate() {
        GiftCard card = newClaimedCard();
        card.charge(30, LocalDateTime.of(2025, 1, 2, 3, 4));
        card.charge(20, LocalDateTime.of(2025, 2, 3, 4, 5));
        assertEquals(VALID_INITIAL_BALANCE - 50, card.getBalance());
        assertEquals(2, card.getTransactions().size());
    }

    @Test public void test09CanNotChargeNonPositiveAmount() {
        GiftCard card = newClaimedCard();
        for (int invalid : new int[]{0, -3}) { //sacar for
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
        assertEquals(2, card.getTransactions().size());
    }

    @Test public void test13GetOwnerNullBeforeClaim() {
        GiftCard card = newUnclaimedCard();
        assertNull(card.getOwner());
    }

    @Test public void test14IsOwnedBy() {
        GiftCard card = newUnclaimedCard();
        card.claimCard(VALID_OWNER);
        assertTrue(card.isOwnedBy(VALID_OWNER));
        assertFalse(card.isOwnedBy("another"));
    }

    @Test public void test15TransactionsListIsUnmodifiable() {
        GiftCard card = newClaimedCard();
        card.charge(5, LocalDateTime.now());
        assertThrows(UnsupportedOperationException.class, () -> card.getTransactions().add(null));
    }
}
// testear que de error la unclaimed.
//testear: balance 0, negativo, charge mayor a balance, get balance en unclaimed, get transactions en unclaimed.
//testear transactions en gral