package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GiftCardTest {

    private static final int INITIAL_BALANCE = 100;
    private static final int CARD_ID = 1;
    private static final int OWNER_ID = 42;
    private static final int CHARGE_AMOUNT = 30;
    private static final int NON_POSITIVE_AMOUNT = 0;
    private static final String INVALID_AMOUNT_MESSAGE = "Invalid amount";
    private static final LocalDateTime T1 = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Test
    public void test01NewGiftCardStartsUnclaimedWithInitialBalance() {
        GiftCard card = new GiftCard(INITIAL_BALANCE, CARD_ID);
        assertFalse(card.claimed);
        assertEquals(INITIAL_BALANCE, card.balance);
    }

    @Test
    public void test02ClaimCardSetsOwnerAndClaimedTrue() {
        GiftCard card = new GiftCard(INITIAL_BALANCE, CARD_ID);
        card.claimCard(OWNER_ID);
        assertTrue(card.claimed);
        assertEquals(OWNER_ID, card.ownerId);
    }

    @Test
    public void test03ChargeReducesBalanceAndAddsTransaction() {
        GiftCard card = new GiftCard(INITIAL_BALANCE, CARD_ID);
        int before = card.balance;
        card.charge(CHARGE_AMOUNT, T1);
        assertEquals(before - CHARGE_AMOUNT, card.balance);
        assertEquals(1, card.transactions.size());
    }

    @Test
    public void test04CanNotChargeNonPositiveAmount() {
        assertThrowsLike(() -> {
            GiftCard card = new GiftCard(INITIAL_BALANCE, CARD_ID);
            card.charge(NON_POSITIVE_AMOUNT, T1);
        }, INVALID_AMOUNT_MESSAGE);
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message,
                assertThrows(Exception.class, executable).getMessage());
    }
}
