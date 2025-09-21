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
        return new SystemFacade(Map.of(VALID_USER, VALID_PASSWORD), Set.of(VALID_MERCHANT_KEY, VALID_MERCHANT_KEY_2), Map.of(VALID_GIFT_CARD_ID, new GiftCard(VALID_GIFT_CARD_BALANCE)), clock);
    }

    public SystemFacade multiUserAndGiftCardSystemFacade(Clock clock) {
        return new SystemFacade(Map.of(VALID_USER, VALID_PASSWORD, VALID_USER_2, VALID_PASSWORD_2), Set.of(VALID_MERCHANT_KEY, VALID_MERCHANT_KEY_2), Map.of(VALID_GIFT_CARD_ID, new GiftCard(VALID_GIFT_CARD_BALANCE), VALID_GIFT_CARD_ID_2, new GiftCard(VALID_GIFT_CARD_BALANCE)), clock);
    }

    @BeforeEach
    public void beforeEach() {
        singleUserSystemFacade = singleUserSystemFacade(new Clock(BASE_TIME));
        multiUserAndGiftCardSystemFacade = multiUserAndGiftCardSystemFacade(new Clock(BASE_TIME));
    }

    @Test
    public void test02CantCreateTokenOnInvalidUser(){
        assertThrowsLike( () -> singleUserSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD),
                SystemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test03CantCreateTokenOnInvalidPassword(){
        assertThrowsLike( () -> singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD_2),
                SystemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test01CantClaimInvalidGiftCard(){
        int token = loginSingleUser();
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token), SystemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test04CanGetGiftCardBalanceOfValidGiftCard(){
        int token = loginAndClaimSingleUser();
        assertEquals(1000, balanceSingleUser(token));
    }

    @Test
    public void test05CantGetGiftCardBalanceOfInvalidGiftCard(){
        int token = loginAndClaimSingleUser();
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID_2, token),
                SystemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test07CantGetGiftCardBalanceOfInvalidToken(){
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, -1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test08CantClaimGiftCardWithInvalidToken(){
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, -1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test10BalanceAfterChargeGiftCard(){
        int token = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(token, 1000);
        chargeSingleUser(200, VALID_MERCHANT_KEY);
        assertBalanceSingleUserEquals(token, 800);
    }

    @Test
    public void test11MerchantCantChargeWithInvalidKey(){
        int token = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(token, 1000);
        assertThrowsLike( () -> chargeSingleUser(200, "InvalidMercadoPagoKey"),
                SystemFacade.invalidMerchantKeyErrorDescription );
    }

    @Test
    public void test12MerchantCantChargeWithInvalidGiftCardID(){
        int token = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(token, 1000);

        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY, VALID_USER),
                SystemFacade.invalidGiftCardIDErrorDescription );
    }

    @Test
    public void test13MerchantCantChargeWithInvalidAmount(){
        int token = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(token, 1000);
        assertThrowsLike( () -> chargeSingleUser(-100, VALID_MERCHANT_KEY),
                GiftCard.InvalidAmountErrorDescription );
    }

    @Test
    public void test14MerchantCantChargeWithUnsufficientBalance(){
        int token = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(token, 1000);
        assertThrowsLike( () -> chargeSingleUser(1001, VALID_MERCHANT_KEY),
                GiftCard.InsufficientBalanceErrorDescription );
    }

    @Test
    public void test15MerchantCantChargeWithInvalidUser(){
        int token = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(token, 1000);
        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(800, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER_2),
                GiftCard.NotOwnerErrorDescription );
    }

    @Test
    public void test16CantGetGiftCardBalanceOfOtherUsersGiftCard(){
        loginAndClaimMultiUser1();
        int token2 = loginMultiUser2();
        assertThrowsLike( () -> multiUserAndGiftCardSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token2),
                GiftCard.NotOwnerErrorDescription );
    }

    @Test
    public void test17CanGetGiftCardBalanceOfOtherUsersGiftCardWithItsToken(){
        int token = loginAndClaimMultiUser1();
        assertEquals( 1000, multiUserAndGiftCardSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token) );
    }

    @Test
    public void test18TransactionHasCorrectAmount(){
        int token = loginAndClaimSingleUser();
        chargeSingleUser(200, VALID_MERCHANT_KEY);
        assertEquals(200, firstTransactionSingleUser(token).getAmount());
    }

    @Test
    public void test19TransactionHasCorrectTime(){
        int token = loginAndClaimSingleUser();
        chargeSingleUser(200, VALID_MERCHANT_KEY);
        assertEquals(BASE_TIME, firstTransactionSingleUser(token).getTime());
    }

    @Test
    public void test20TransactionHasCorrectMerchantKey(){
        int token = loginAndClaimSingleUser();
        chargeSingleUser(200, VALID_MERCHANT_KEY);
        assertEquals(VALID_MERCHANT_KEY, firstTransactionSingleUser(token).getMerchantKey());
    }

    @Test
    public void test21MultipleTransactionStoresChargesCorrectly() {
        int token = loginAndClaimSingleUser();
        chargeSingleUser(200, VALID_MERCHANT_KEY);
        chargeSingleUser(400, VALID_MERCHANT_KEY_2);
        chargeSingleUser(100, VALID_MERCHANT_KEY_2);

        List<Transaction> userTransactions = transactionsSingleUser(token);
        assertEquals(3, userTransactions.size());
        assertEquals(200, userTransactions.getFirst().getAmount());
        assertEquals(400, userTransactions.get(1).getAmount());
        assertEquals(100, userTransactions.get(2).getAmount());
    }


    @Test
    public void test22MultipleGiftCardsChargeTransactionsCorrectly() {
        int token = loginAndClaimBothCardsSingleUser();
        chargeMulti(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        chargeMulti(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER);

        assertEquals(1, transactionsMulti(VALID_GIFT_CARD_ID, token).size());
        assertEquals(1, transactionsMulti(VALID_GIFT_CARD_ID_2, token).size());
        assertEquals(200, transactionsMulti(VALID_GIFT_CARD_ID, token).getFirst().getAmount());
        assertEquals(400, transactionsMulti(VALID_GIFT_CARD_ID_2, token).getFirst().getAmount());
    }

    @Test
    public void test23MultipleUsersWithMultipleGiftCardsStoreTransactionsAmountsCorrectly() {
        int[] ts = loginAndClaimBothUsers();
        chargeBothUsersDefault();
        assertFirstAmountsDefault(ts[0], ts[1]);
    }

    @Test
    public void test24MultipleUsersWithMultipleGiftCardsStoreMerchantKeysCorrectly() {
        int[] ts = loginAndClaimBothUsers();
        chargeBothUsersDefault();
        assertFirstKeysDefault(ts[0], ts[1]);
    }


    @Test
    public void test25MultipleUsersWithMultipleGiftCardsStoreTransactionsTimeCorrectly() {
        int[] ts = loginAndClaimBothUsers();
        chargeBothUsersDefault();
        assertFirstTimesDefault(ts[0], ts[1]);
    }

    @Test
    public void test26MultipleUsersWithMultipleGiftCardsStoreTransactionsCorrectly() {
        int[] ts = loginAndClaimBothUsers();
        chargeBothUsersDefault();
        assertTransactionsSizeBoth(ts[0], ts[1], 1);
    }

    @Test
    public void test27CantGetTransactionsOfAnotherUser() {
        loginAndClaimMultiUser1();
        int token2 = loginMultiUser2();
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY,  VALID_USER);

        assertThrowsLike(() -> multiUserAndGiftCardSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token2),
                GiftCard.NotOwnerErrorDescription);
    }

    @Test
    public void test28TokenSessionExpiresAfterFiveMinutesWhenClaimingGiftCard() {
        Clock clock = seqClock(BASE_TIME, BASE_TIME.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token),
                SystemFacade.sessionHasExpiredErrorDescription);
    }

    @Test
    public void test29TokenSessionExpiresAfterFiveMinutesWhenGettingBalance() {
        Clock clock = seqClock(BASE_TIME, BASE_TIME, BASE_TIME, BASE_TIME.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token),
                SystemFacade.sessionHasExpiredErrorDescription);
    }

    @Test
    public void test30TokenSessionExpiresAfterFiveMinutesWhenGettingTransactions() {
        Clock clock = seqClock(BASE_TIME, BASE_TIME, BASE_TIME, BASE_TIME.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertThrowsLike( () -> singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token),
                SystemFacade.sessionHasExpiredErrorDescription);
    }

    @Test
    public void test31TokenSessionDoesNotExpireWhenNotInactive() {
        Clock clock = seqClock(BASE_TIME, BASE_TIME, BASE_TIME.plusMinutes(2), BASE_TIME.plusMinutes(6), BASE_TIME.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int token = singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        assertDoesNotThrow( () -> singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token));
    }

    private void assertThrowsLike(Executable executable, String message) {assertEquals(message, assertThrows(Exception.class, executable).getMessage());}

    private int loginAndClaimSingleUser() {
        int token = loginSingleUser();
        singleUserSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        return token;
    }

    private int loginAndClaimMultiUser1() {
        int token = loginMultiUser1();
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        return token;
    }

    private int loginAndClaimMultiUser2() {
        int token = loginMultiUser2();
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token);
        return token;
    }

    private int loginSingleUser() {return singleUserSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);}    
    private void chargeSingleUser(int amount, String merchantKey) {singleUserSystemFacade.chargeGiftCard(amount, VALID_GIFT_CARD_ID, merchantKey, VALID_USER);}    
    private int balanceSingleUser(int token) {return singleUserSystemFacade.getGiftCardBalance(VALID_GIFT_CARD_ID, token);}    
    private void assertBalanceSingleUserEquals(int token, int expected) {assertEquals(expected, balanceSingleUser(token));}
    private List<Transaction> transactionsSingleUser(int token) {return singleUserSystemFacade.getTransactionsFor(VALID_GIFT_CARD_ID, token);}    
    private Transaction firstTransactionSingleUser(int token) {return transactionsSingleUser(token).get(0);}    
    private int loginMultiUser1() {return multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER, VALID_PASSWORD);}    
    private int loginMultiUser2() {return multiUserAndGiftCardSystemFacade.createSessionFor(VALID_USER_2, VALID_PASSWORD_2);}    
    private int loginAndClaimBothCardsSingleUser() {
        int token = loginMultiUser1();
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(VALID_GIFT_CARD_ID_2, token);
        return token;
    }
    private void chargeMulti(int amount, Integer giftCardId, String merchantKey, String userName) {multiUserAndGiftCardSystemFacade.chargeGiftCard(amount, giftCardId, merchantKey, userName);}    
    private List<Transaction> transactionsMulti(int giftCardId, int token) {return multiUserAndGiftCardSystemFacade.getTransactionsFor(giftCardId, token);}    
    private Transaction firstTransactionMulti(int giftCardId, int token) {return transactionsMulti(giftCardId, token).get(0);}    
    private Clock seqClock(LocalDateTime... times) {
        return new Clock(times[0]) {
            Iterator<LocalDateTime> seq = java.util.List.of(times).iterator();
            public LocalDateTime getTime() { return seq.next(); }
        };
    }

    private int[] loginAndClaimBothUsers() {
        int t1 = loginAndClaimMultiUser1();
        int t2 = loginAndClaimMultiUser2();
        return new int[]{t1, t2};
    }

    private void chargeBothUsersDefault() {
        chargeMulti(200, VALID_GIFT_CARD_ID, VALID_MERCHANT_KEY, VALID_USER);
        chargeMulti(400, VALID_GIFT_CARD_ID_2, VALID_MERCHANT_KEY_2, VALID_USER_2);
    }

    private void assertFirstAmountsDefault(int t1, int t2) {
        assertEquals(200, firstTransactionMulti(VALID_GIFT_CARD_ID, t1).getAmount());
        assertEquals(400, firstTransactionMulti(VALID_GIFT_CARD_ID_2, t2).getAmount());
    }

    private void assertFirstKeysDefault(int t1, int t2) {
        assertEquals(VALID_MERCHANT_KEY, firstTransactionMulti(VALID_GIFT_CARD_ID, t1).getMerchantKey());
        assertEquals(VALID_MERCHANT_KEY_2, firstTransactionMulti(VALID_GIFT_CARD_ID_2, t2).getMerchantKey());
    }

    private void assertFirstTimesDefault(int t1, int t2) {
        assertEquals(BASE_TIME, firstTransactionMulti(VALID_GIFT_CARD_ID, t1).getTime());
        assertEquals(BASE_TIME, firstTransactionMulti(VALID_GIFT_CARD_ID_2, t2).getTime());
    }

    private void assertTransactionsSizeBoth(int t1, int t2, int expected) {
        assertEquals(expected, transactionsMulti(VALID_GIFT_CARD_ID, t1).size());
        assertEquals(expected, transactionsMulti(VALID_GIFT_CARD_ID_2, t2).size());
    }
}
