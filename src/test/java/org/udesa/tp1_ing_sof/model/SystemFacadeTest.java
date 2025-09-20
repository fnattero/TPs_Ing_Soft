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
    private static final String VALID_USER_2 = "Lucho";
    private static final String VALID_PASSWORD_2 = "Contra_de_lucho";
    private static final Integer VALID_GIFT_CARD_ID = 12345678;
    private static final Integer VALID_GIFT_CARD_ID_2 = 12345679;
    private static final Integer VALID_GIFT_CARD_BALANCE = 1000;
    private static final String VALID_MERCHANT_KEY = "MercadoPagoKey";
    private static final String VALID_MERCHANT_KEY_2 = "ModoKey";

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 1, 2, 3, 0, 0);

    SystemFacade singleUserSystemFacade;
    SystemFacade multiUserAndGiftCardSystemFacade;

    public SystemFacade singleUserSystemFacade(Clock clock) {
        return new SystemFacade(
                Map.of(VALID_USER, VALID_PASSWORD),
                Set.of(VALID_MERCHANT_KEY, VALID_MERCHANT_KEY_2),
                Map.of(VALID_GIFT_CARD_ID, new GiftCard(VALID_GIFT_CARD_BALANCE)),
                clock
        );
    }

    public SystemFacade multiUserAndGiftCardSystemFacade(Clock clock) {
        return new SystemFacade(
                Map.of(VALID_USER, VALID_PASSWORD, VALID_USER_2, VALID_PASSWORD_2),
                Set.of(VALID_MERCHANT_KEY, VALID_MERCHANT_KEY_2),
                Map.of(VALID_GIFT_CARD_ID, new GiftCard(VALID_GIFT_CARD_BALANCE), VALID_GIFT_CARD_ID_2, new GiftCard(VALID_GIFT_CARD_BALANCE)),
                clock
        );
    }

    @BeforeEach
    public void beforeEach() {
        singleUserSystemFacade = singleUserSystemFacade(new Clock(BASE_TIME));
        multiUserAndGiftCardSystemFacade = multiUserAndGiftCardSystemFacade(new Clock(BASE_TIME));
    }

    @Test
    public void test02CantCreateTokenOnInvalidUser(){
        assertThrowsLike( () -> singleUserSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD),
                singleUserSystemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test03CantCreateTokenOnInvalidPassword(){
        assertThrowsLike( () -> singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD_2),
                singleUserSystemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void testcantClaimInvalidGiftCard(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token), SystemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test04CanGetGiftCardBalanceOfValidGiftCard(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));
    }

    @Test
    public void test05CantGetGiftCardBalanceOfInvalidGiftCard(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID_2, token),
                singleUserSystemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test07CantGetGiftCardBalanceOfInvalidToken(){
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, -1),
                singleUserSystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test08CantClaimGiftCardWithInvalidToken(){
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, -1),
                singleUserSystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test10BalanceAfterChargeGiftCard(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);

        assertEquals(800, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));
    }

    @Test
    public void test11MerchantCantChargeWithInvalidKey(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, "InvalidMercadoPagoKey", VALID_USER),
                singleUserSystemFacade.invalidMerchantKeyErrorDescription );
    }

    @Test
    public void test12MerchantCantChargeWithInvalidGiftCardID(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY, VALID_USER),
                singleUserSystemFacade.invalidGiftCardIDErrorDescription );
    }

    @Test
    public void test13MerchantCantChargeWithInvalidAmount(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(-100, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER),
                GiftCard.InvalidAmountErrorDescription );
    }

    @Test
    public void testMerchantCantChargeWithUnsufficientBalance(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(1001, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER),
                GiftCard.InsufficientBalanceErrorDescription );
    }

    @Test
    public void testMerchantCantChargeWithInvalidUser(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertEquals(1000, singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token));

        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(800, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER_2),
                GiftCard.NotOwnerErrorDescription );
    }

    @Test
    public void testCantGetGiftCardBalanceOfOtherUsersGiftCard(){
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);

        assertThrowsLike( () -> multiUserAndGiftCardSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token2),
                GiftCard.NotOwnerErrorDescription );
    }

    @Test
    public void testCanGetGiftCardBalanceOfOtherUsersGiftCardWithItsToken(){
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);

        assertEquals( 1000, multiUserAndGiftCardSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token) );
    }

    @Test
    public void testTransactionHasCorrectAmount(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);

        assertEquals(200, singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token)
                                    .get(0)
                                    .getAmount());
    }

    @Test
    public void testTransactionHasCorrectTime(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        assertEquals(BASE_TIME,  singleUserSystemFacade
                                .getTransactionsFor(VALID_GIFT_CARD_ID, token)
                                .get(0)
                                .getTime());
    }

    @Test
    public void testTransactionHasCorrectMerchantKey(){
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        assertEquals(VALID_MERCHANT_KEY,  singleUserSystemFacade
                                        .getTransactionsFor(VALID_GIFT_CARD_ID, token)
                                        .get(0)
                                        .getMerchantKey());
    }

    @Test
    public void testMultipleTransactionStoresChargesCorrectly() {
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        singleUserSystemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY_2, VALID_USER);
        singleUserSystemFacade.chargeGiftCard(100, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY_2, VALID_USER);

        List<Transaction> userTransactions = singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token);
        assertEquals(3, userTransactions.size());
        assertEquals(200, userTransactions.getFirst().getAmount());
        assertEquals(400, userTransactions.get(1).getAmount());
        assertEquals(100, userTransactions.get(2).getAmount());
    }


    @Test
    public void testMultipleGiftCardsChargeTransactionsCorrectly() {
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER);

        assertEquals(1, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token).size());
        assertEquals(1, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID_2, token).size());
        assertEquals(200, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token).getFirst().getAmount());
        assertEquals(400, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID_2, token).getFirst().getAmount());
    }

    @Test
    public void testMultipleUsersWithMultipleGiftCardsStoreTransactionsAmountsCorrectly() {
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token2);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER_2);

        assertEquals(200, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token).getFirst().getAmount());
        assertEquals(400, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID_2, token2).getFirst().getAmount());
    }

    @Test
    public void testMultipleUsersWithMultipleGiftCardsStoreMerchantKeysCorrectly() {
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token2);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER_2);

        assertEquals(VALID_MERCHANT_KEY, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token).getFirst().getMerchantKey());
        assertEquals(VALID_MERCHANT_KEY_2, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID_2, token2).getFirst().getMerchantKey());
    }


    @Test
    public void testMultipleUsersWithMultipleGiftCardsStoreTransactionsTimeCorrectly() {
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token2);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER_2);

        assertEquals(BASE_TIME, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token).getFirst().getTime());
        assertEquals(BASE_TIME, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID_2, token2).getFirst().getTime());
    }

    @Test
    public void testMultipleUsersWithMultipleGiftCardsStoreTransactionsCorrectly() {
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token2);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER_2);

        assertEquals(1, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token).size());
        assertEquals(1, multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID_2, token2).size());
    }

    @Test
    public void testCantGetTransactionsOfAnotherUser() {
        int token = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        int token2 = multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY,  VALID_USER);

        assertThrowsLike(() -> multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token2),
                GiftCard.NotOwnerErrorDescription);
    }

    @Test
    public void testTokenSessionExpiresAfterFiveMinutesWhenClaimingGiftCard() {
        Clock clock = new Clock(BASE_TIME) {
            Iterator<LocalDateTime> seq = java.util.List.of(
                    BASE_TIME,
                    BASE_TIME.plusMinutes(6)
            ).iterator();
            public LocalDateTime getTime() {
                return seq.next();
            }
        };

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token),
                singleUserSystemFacade.sessionHasExpiredErrorDescription);
    }

    @Test
    public void testTokenSessionExpiresAfterFiveMinutesWhenGettingBalance() {
        Clock clock = new Clock(BASE_TIME) {
            Iterator<LocalDateTime> seq = java.util.List.of(
                    BASE_TIME,
                    BASE_TIME,
                    BASE_TIME,
                    BASE_TIME.plusMinutes(6)
            ).iterator();
            public LocalDateTime getTime() {
                return seq.next();
            }
        };

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token),
                singleUserSystemFacade.sessionHasExpiredErrorDescription);
    }

    @Test
    public void testTokenSessionExpiresAfterFiveMinutesWhenGettingTransactions() {
        Clock clock = new Clock(BASE_TIME) {
            Iterator<LocalDateTime> seq = java.util.List.of(
                    BASE_TIME,
                    BASE_TIME,
                    BASE_TIME,
                    BASE_TIME.plusMinutes(6)
            ).iterator();
            public LocalDateTime getTime() {
                return seq.next();
            }
        };

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token),
                singleUserSystemFacade.sessionHasExpiredErrorDescription);
    }

    @Test
    public void testTokenSessionDoesNotExpireWhenNotInactive() {
        Clock clock = new Clock(BASE_TIME) {
            Iterator<LocalDateTime> seq = java.util.List.of(
                    BASE_TIME,
                    BASE_TIME,
                    BASE_TIME.plusMinutes(2),
                    BASE_TIME.plusMinutes(6),
                    BASE_TIME.plusMinutes(6)
            ).iterator();
            public LocalDateTime getTime() {
                return seq.next();
            }
        };

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertDoesNotThrow( () -> singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token));
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
