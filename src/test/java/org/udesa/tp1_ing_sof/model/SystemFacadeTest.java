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

//    @Test
//    public void test01ValidTokenIsCreated(){
//        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
//        assertTrue(systemFacade.checkTokenSessionIsActive(token));
//    }

    @Test
    public void test02CantCreateTokenOnInvalidUser(){
        assertThrowsLike( () -> systemFacade.createSessionFor(INVALID_USER, VALID_PASSWORD), systemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test03CantCreateTokenOnInvalidPassword(){
        assertThrowsLike( () -> systemFacade.createSessionFor(VALID_USER, INVALID_PASSWORD), systemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test04CanGetGiftCardBalanceOfValidGiftCard(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));
    }

    @Test
    public void test05CantGetGiftCardBalanceOfInvalidGiftCard(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> systemFacade.getGiftCardBalance(INVALID_GIFT_CARD_ID, token), systemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test07CantGetGiftCardBalanceOfInvalidToken(){
        assertThrowsLike( () -> systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, -1), systemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test08CantClaimGiftCardWithInvalidToken(){
        assertThrowsLike( () -> systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, -1), systemFacade.invalidTokenErrorDescription);
    }
    //cant claim invalid gift card

    @Test
    public void test10BalanceAfterChargeGiftCard(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        systemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "MercadoPago_key");

        assertEquals(800, systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));
    }

    @Test
    public void test11MerchantCantChargeWithInvalidKey(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> systemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "Invalid_MercadoPago_key"), systemFacade.invalidMerchantKeyErrorDescription );
    }
    //register sale fails,menos precio, mal precio

    @Test
    public void test12MerchantCantChargeWithInvalidGiftCardID(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> systemFacade.chargeGiftCard(200, INVALID_GIFT_CARD_ID, "MercadoPago_key"), systemFacade.invalidGiftCardIDErrorDescription );
    }

    @Test
    public void test13MerchantCantChargeWithInvalidAmount(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> systemFacade.chargeGiftCard(-100, VALID_GIFT_CARD_ID, "MercadoPago_key"), systemFacade.invalidMerchantChargeAmountErrorDescription );
    }

    @Test
    public void testCantGetGiftCardBalanceOfOtherUsersGiftCard(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = systemFacade.createSessionFor("Lucho", "Contra_de_lucho");
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);

        assertThrowsLike( () -> systemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token2), GiftCard.giftCardBelongsToAnotherUser );
    }

    @Test
    public void testMultipleTransactionStoresChargesCorrectly(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        systemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "MercadoPago_key");
        systemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID, "Modo_key");
        systemFacade.chargeGiftCard(100, VALID_GIFT_CARD_ID, "Modo_key");

        List<Transaction> userTransactions = systemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token);
        assertEquals(3, userTransactions.size());
        assertEquals(userTransactions.getFirst().getTransactionCharge(), 200);
        assertEquals(userTransactions.get(1).getTransactionCharge(), 400);
        assertEquals(userTransactions.get(2).getTransactionCharge(), 100);
    }


    @Test
    public void testMultipleGiftCardsChargeTransactionsCorrectly(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID + 1, token);
        systemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "MercadoPago_key");
        systemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID + 1, "Modo_key");

        List<Transaction> userTransactionsCard1 = systemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token);
        List<Transaction> userTransactionsCard2 = systemFacade.getTransactionsFor(VALID_GIFT_CARD_ID + 1, token);
        assertEquals(1, userTransactionsCard1.size());
        assertEquals(1, userTransactionsCard2.size());
        assertEquals(userTransactionsCard1.getFirst().getTransactionCharge(), 200);
        assertEquals(userTransactionsCard1.getFirst().getTransactionCharge(), 400);
    }

    @Test
    public void testMultipleUsersWithMultipleGiftCardsChargeTransactionsCorrectly(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = systemFacade.createSessionFor("Lucho", "Contra_de_lucho");
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID + 1, token2);
        systemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "MercadoPago_key");
        systemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID + 1, "Modo_key");

        List<Transaction> userTransactionsCard1 = systemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token);
        List<Transaction> userTransactionsCard2 = systemFacade.getTransactionsFor(VALID_GIFT_CARD_ID + 1, token2);
        assertEquals(1, userTransactionsCard1.size());
        assertEquals(1, userTransactionsCard2.size());
        assertEquals(userTransactionsCard1.getFirst().getTransactionCharge(), 200);
        assertEquals(userTransactionsCard1.getFirst().getTransactionCharge(), 400);
    }

    @Test
    public void testCantGetTransactionsOfAnotherUser(){
        int token = systemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = systemFacade.createSessionFor("Lucho", "Contra_de_lucho");
        systemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        systemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "MercadoPago_key");

        assertThrowsLike( systemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token2), GiftCard.giftCardBelongsToAnotherUser);
    }



    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message,
                assertThrows(Exception.class, executable)
                        .getMessage());
    }
}

//checkear que no se pueda operar en una tarjeta agena. Checkear transactions de user. sale updates balance. checkear user en charge
// se puede operar con dos tarjetas.
//test con dos usuarios.

//preguntar por el override. Preguntar si el charge de merchant tiene que mandar username.

//sacasr override
//pasar username
// chequear que un user con el token de otro pueda ver su balance
