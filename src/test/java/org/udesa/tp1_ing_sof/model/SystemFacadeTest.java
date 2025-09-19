package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class SystemFacadeTest {

    private static final String VALID_USER = "Juan";
    private static final String VALID_PASSWORD = "Contra_de_juan";
    private static final String INVALID_USER = "Lucho";
    private static final String INVALID_PASSWORD = "Contra_de_lucho";
    private static final Integer VALID_GIFT_CARD_ID = 12345678;
    private static final Integer INVALID_GIFT_CARD_ID = 123456789;
    private static final Integer VALID_GIFT_CARD_BALANCE = 1000;

    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    SystemFacade systemFacade;

    @BeforeEach
    public void beforeEach() {
        systemFacade = new SystemFacade(
                Map.of(VALID_USER, VALID_PASSWORD),
                Set.of("MercadoPago_key", "Modo_key"),
                Map.of(VALID_GIFT_CARD_ID, new GiftCard(VALID_GIFT_CARD_BALANCE)),
                new Clock(BASE)
        );
    }

    @Test
    public void test01ValidTokenIsCreated(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        assertTrue(systemFacade.sessionExists(token));
    }

    @Test
    public void test02CantCreateTokenOnInvalidUser(){
        assertThrowsLike( () -> systemFacade.createSessionFor(INVALID_USER, VALID_PASSWORD), systemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test03CantCreateTokenOnInvalidPassword(){
        assertThrowsLike( () -> systemFacade.createSessionFor(VALID_USER, INVALID_PASSWORD), systemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test04CanCheckBalanceOfValidGiftCard(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.checkBalance(VALID_GIFT_CARD_ID, token));
    }

    @Test
    public void test05CantCheckBalanceOfInvalidGiftCard(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> systemFacade.checkBalance(INVALID_GIFT_CARD_ID, token), systemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test07CantCheckBalanceOfInvalidToken(){
        assertThrowsLike( () -> systemFacade.checkBalance(VALID_GIFT_CARD_ID, -1), systemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test08CantClaimGiftCardWithInvalidToken(){
        assertThrowsLike( () -> systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, -1), systemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test10BalanceAfterMerchantCharge(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.checkBalance(VALID_GIFT_CARD_ID, token));

        systemFacade.merchantCharge(200, VALID_GIFT_CARD_ID, "MercadoPago_key");

        assertEquals(800, systemFacade.checkBalance(VALID_GIFT_CARD_ID, token));
    }

    @Test
    public void test11MerchantCantChargeWithInvalidKey(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.checkBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> systemFacade.merchantCharge(200, VALID_GIFT_CARD_ID, "Invalid_MercadoPago_key"), systemFacade.invalidMerchantKeyErrorDescription );
    }

    @Test
    public void test12MerchantCantChargeWithInvalidGiftCardID(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.checkBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> systemFacade.merchantCharge(200, INVALID_GIFT_CARD_ID, "MercadoPago_key"), systemFacade.invalidGiftCardIDErrorDescription );
    }

    @Test
    public void test13MerchantCantChargeWithInvalidAmount(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.checkBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> systemFacade.merchantCharge(-100, VALID_GIFT_CARD_ID, "MercadoPago_key"), systemFacade.invalidMerchantChargeAmountErrorDescription );
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message,
                assertThrows(Exception.class, executable)
                        .getMessage());
    }
}
