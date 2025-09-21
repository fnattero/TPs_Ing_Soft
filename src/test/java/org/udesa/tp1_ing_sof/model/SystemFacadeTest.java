package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class SystemFacadeTest {

    private static final String User1 = "Juan";
    private static final String PasswordUser1 = "Contra_de_juan";
    private static final String User2 = "Lucho";
    private static final String PasswordUser2 = "Contra_de_lucho";
    private static final Integer GiftCardIdUser1 = 12345678;
    private static final Integer GiftCardIdUser2 = 12345679;
    private static final Integer InitialGiftCardBalance = 1000;
    private static final String ValidMerchantKey = "MercadoPagoKey";
    private static final String ValidMerchantKey2 = "ModoKey";

    private static final LocalDateTime BaseTime = LocalDateTime.of(2025, 1, 2, 3, 0, 0);

    SystemFacade singleUserSystemFacade;
    SystemFacade multiUserAndGiftCardSystemFacade;

    public SystemFacade singleUserSystemFacade(Clock clock) {
        return new SystemFacade(Map.of(User1, PasswordUser1), Set.of(ValidMerchantKey, ValidMerchantKey2), Map.of(GiftCardIdUser1, new GiftCard(InitialGiftCardBalance)), clock);
    }

    public SystemFacade multiUserAndGiftCardSystemFacade(Clock clock) {
        return new SystemFacade(Map.of(User1, PasswordUser1, User2, PasswordUser2), Set.of(ValidMerchantKey, ValidMerchantKey2), Map.of(GiftCardIdUser1, new GiftCard(InitialGiftCardBalance), GiftCardIdUser2, new GiftCard(InitialGiftCardBalance)), clock);
    }

    @BeforeEach
    public void beforeEach() {
        singleUserSystemFacade = singleUserSystemFacade(new Clock());
        multiUserAndGiftCardSystemFacade = multiUserAndGiftCardSystemFacade(new Clock());
    }

    @Test
    public void test01CantClaimInvalidGiftCard(){
        int tokenUser1 = loginSingleUser();
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(GiftCardIdUser2, tokenUser1),
                SystemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test02CantClaimGiftCardWithInvalidToken(){
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, -1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test03CantCreateTokenOnInvalidUser(){
        assertThrowsLike( () -> singleUserSystemFacade.createSessionFor(User2, PasswordUser1),
                SystemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test04CantCreateTokenOnInvalidPassword(){
        assertThrowsLike( () -> singleUserSystemFacade.createSessionFor(User1, PasswordUser2),
                SystemFacade.invalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test05CanGetGiftCardBalanceOfValidGiftCard(){
        int tokenUser1 = loginAndClaimSingleUser();
        assertEquals(1000, getBalanceSingleUser(tokenUser1));
    }

    @Test
    public void test06CantGetGiftCardBalanceOfInvalidGiftCard(){
        int tokenUser1 = loginAndClaimSingleUser();
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(GiftCardIdUser2, tokenUser1),
                SystemFacade.invalidGiftCardIDErrorDescription);
    }

    @Test
    public void test07CantGetGiftCardBalanceOfInvalidToken(){
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(GiftCardIdUser1, -1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test08CantGetGiftCardBalanceOfOtherUsersGiftCard(){
        loginAndClaimMultiUser1();
        int tokenUser2 = loginMultiUser2();
        assertThrowsLike( () -> multiUserAndGiftCardSystemFacade.getGiftCardBalance(GiftCardIdUser1, tokenUser2),
                GiftCard.NotOwnerErrorDescription );
    }

    @Test
    public void test09CantGetGiftCardBalanceOfUnclaimedGiftCard(){
        int tokenUser1 = loginAndClaimMultiUser1();
        assertThrowsLike( () -> multiUserAndGiftCardSystemFacade.getGiftCardBalance(GiftCardIdUser2, tokenUser1),
                GiftCard.CantGetBalanceOfUnclaimedCardErrorDescription );
    }

    @Test
    public void test10SingleUserChargesUpdateBalanceCorrectly(){
        int tokenUser1 = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(tokenUser1, 1000);
        chargeSingleUser(200, ValidMerchantKey);
        assertBalanceSingleUserEquals(tokenUser1, 800);
    }

    @Test
    public void test11MultipleUserChargesUpdateBalanceCorrectly(){
        int[] userTokens = loginAndClaimBothUsers();
        chargeBothUsers(100, 200);
        chargeBothUsers(300, 600);
        assertEquals(InitialGiftCardBalance - 400, multiUserAndGiftCardSystemFacade.getGiftCardBalance(GiftCardIdUser1, userTokens[0]));
        assertEquals(InitialGiftCardBalance - 800, multiUserAndGiftCardSystemFacade.getGiftCardBalance(GiftCardIdUser2, userTokens[1]));
    }

    @Test
    public void test12CanGetGiftCardBalanceOfOtherUsersGiftCardWithItsToken(){
        // Este test puede parecer redundante pero es para aclarar un aspecto del funcionamiento del modelo. Queremos mostrar que
        // si el usuario 2 chequeara el balance usando el token del usuario 1 el sistema le permitiria verlo. Hay que imaginar
        // que el que esta llamando a getBalance es el usuario 2 con el token del usuario 1.
        int[] userTokens = loginAndClaimBothUsers();
        assertEquals( 1000, multiUserAndGiftCardSystemFacade.getGiftCardBalance(GiftCardIdUser1, userTokens[0]) );
    }

    @Test
    public void test13MerchantCantChargeWithInvalidAmount(){
        int tokenUser1 = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(tokenUser1, 1000);
        assertThrowsLike( () -> chargeSingleUser(-100, ValidMerchantKey),
                GiftCard.InvalidAmountErrorDescription );
    }

    @Test
    public void test14MerchantCantChargeWithUnsufficientBalance(){
        int tokenUser1 = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(tokenUser1, 1000);
        assertThrowsLike( () -> chargeSingleUser(1001, ValidMerchantKey),
                GiftCard.InsufficientBalanceErrorDescription );
    }

    @Test
    public void test15MerchantCantChargeWithInvalidGiftCardID(){
        loginAndClaimSingleUser();
        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(200, GiftCardIdUser2, ValidMerchantKey, User1),
                SystemFacade.invalidGiftCardIDErrorDescription );
    }

    @Test
    public void test16MerchantCantChargeUnclaimedGiftCard(){
        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(200, GiftCardIdUser1, ValidMerchantKey, User1),
                GiftCard.CantChargeUnclaimedCardErrorDescription);
    }

    @Test
    public void test17MerchantCantChargeWithInvalidKey(){
        int tokenUser1 = loginAndClaimSingleUser();
        assertBalanceSingleUserEquals(tokenUser1, 1000);
        assertThrowsLike( () -> chargeSingleUser(200, "InvalidMercadoPagoKey"),
                SystemFacade.invalidMerchantKeyErrorDescription );
    }

    @Test
    public void test18MerchantCantChargeWithInvalidUser(){
        assertThrowsLike( () -> singleUserSystemFacade.chargeGiftCard(800, GiftCardIdUser1, ValidMerchantKey, User2),
                SystemFacade.invalidUserAndOrPasswordErrorDescription );
    }

    @Test
    public void test19MerchantCantChargeGiftCardOwnedByAnotherUser(){
        loginAndClaimBothUsers();
        assertThrowsLike( () -> multiUserAndGiftCardSystemFacade.chargeGiftCard(800, GiftCardIdUser1, ValidMerchantKey, User2),
                GiftCard.NotOwnerErrorDescription );
    }

    @Test
    public void test20TransactionHasCorrectAmount(){
        int tokenUser1 = loginAndClaimSingleUser();
        chargeSingleUser(200, ValidMerchantKey);
        assertEquals(200, getFirstTransactionSingleUser(tokenUser1).getAmount());
    }

    @Test
    public void test21TransactionHasCorrectTime(){
        Clock clock = seqClock(BaseTime, BaseTime, BaseTime, BaseTime, BaseTime, BaseTime);
        singleUserSystemFacade = singleUserSystemFacade(clock);
        int tokenUser1 = loginAndClaimSingleUser(); //3 clocks: create session, isExpired en claim y updateLastAccess en claim
        chargeSingleUser(200, ValidMerchantKey); //Un clock, para pasarle a transaction
        assertEquals(BaseTime, getFirstTransactionSingleUser(tokenUser1).getTime()); //2 clocks, isExpired y updateLastAcces en Transaction
    }

    @Test
    public void test22TransactionHasCorrectMerchantKey(){
        int tokenUser1 = loginAndClaimSingleUser();
        chargeSingleUser(200, ValidMerchantKey);
        assertEquals(ValidMerchantKey, getFirstTransactionSingleUser(tokenUser1).getMerchantKey());
    }

    @Test
    public void test23CantGetTransactionsOfInvalidGiftCardID(){
        int tokenUser1 = loginAndClaimSingleUser();
        chargeSingleUser(200, ValidMerchantKey);
        assertThrowsLike( () -> singleUserSystemFacade.getTransactionsFor( GiftCardIdUser2, tokenUser1),
                SystemFacade.invalidGiftCardIDErrorDescription
        );
    }

    @Test
    public void test24CantGetTransactionsOfAnotherUser() {
        loginAndClaimMultiUser1();
        int tokenUser2 = loginMultiUser2();
        multiUserAndGiftCardSystemFacade.chargeGiftCard(200, GiftCardIdUser1, ValidMerchantKey, User1);

        assertThrowsLike(() -> multiUserAndGiftCardSystemFacade.getTransactionsFor(GiftCardIdUser1, tokenUser2),
                GiftCard.NotOwnerErrorDescription);
    }

    @Test
    public void test25CantGetTransactionsOfUnclaimedCard() {
        int tokenUser1 = loginAndClaimMultiUser1();
        assertThrowsLike( ()-> multiUserAndGiftCardSystemFacade.getTransactionsFor(GiftCardIdUser2, tokenUser1),
                GiftCard.CantGetTransactionsOfUnclaimedCardErrorDescription
        );
    }

    @Test
    public void test26SingleUserSingleGiftCardChargeTransactionsCorrectly() {
        int tokenUser1 = loginAndClaimSingleUser();
        chargeSingleUser(200, ValidMerchantKey);
        chargeSingleUser(400, ValidMerchantKey2);
        chargeSingleUser(100, ValidMerchantKey2);

        List<Transaction> userTransactions = getTransactionsSingleUser(tokenUser1);
        assertEquals(3, userTransactions.size());
        assertEquals(200, userTransactions.getFirst().getAmount());
        assertEquals(400, userTransactions.get(1).getAmount());
        assertEquals(100, userTransactions.get(2).getAmount());
    }

    @Test
    public void test27SingleUserMultipleGiftCardsChargeTransactionsCorrectly() {
        int tokenUser1 = loginAndClaimBothCardsSingleUser();
        chargeMulti(200, GiftCardIdUser1, ValidMerchantKey, User1);
        chargeMulti(400, GiftCardIdUser2, ValidMerchantKey2, User1);

        assertEquals(1, getTransactionsMulti(GiftCardIdUser1, tokenUser1).size());
        assertEquals(1, getTransactionsMulti(GiftCardIdUser2, tokenUser1).size());
        assertEquals(200, getTransactionsMulti(GiftCardIdUser1, tokenUser1).getFirst().getAmount());
        assertEquals(400, getTransactionsMulti(GiftCardIdUser2, tokenUser1).getFirst().getAmount());
    }

    @Test
    public void test28MultipleUsersMultipleGiftCardsStoreTransactionsAmountsCorrectly() {
        int[] userTokens = loginAndClaimBothUsers();
        chargeBothUsers(200, 400);
        assertTransactionsFirstAmounts(userTokens[0], userTokens[1], 200, 400);
    }

    @Test
    public void test29MultipleUsersWithMultipleGiftCardsStoreMerchantKeysCorrectly() {
        int[] userTokens = loginAndClaimBothUsers();
        chargeBothUsers(200, 400);
        assertTransactionsFirstKeysDefault(userTokens[0], userTokens[1]);
    }

    @Test
    public void test30MultipleUsersWithMultipleGiftCardsStoreTransactionsTimeCorrectly() {
        Clock clock = seqClock(BaseTime, BaseTime, BaseTime, BaseTime, BaseTime, BaseTime, BaseTime,
                BaseTime, BaseTime, BaseTime, BaseTime, BaseTime);
        multiUserAndGiftCardSystemFacade = multiUserAndGiftCardSystemFacade(clock);
        int[] userTokens = loginAndClaimBothUsers();
        chargeBothUsers(200, 400);
        assertTransactionsFirstTimesDefault(userTokens[0], userTokens[1]);
    }

    @Test
    public void test31MultipleUsersWithMultipleGiftCardsHaveCorrectTransactionsSize() {
        int[] userTokens = loginAndClaimBothUsers();
        chargeBothUsers(200, 400);
        assertTransactionsSizeBoth(userTokens[0], userTokens[1], 1);
    }

    @Test
    public void test32TokenSessionExpiresAndCeasesToExistAfterFiveMinutesWhenClaimingGiftCard() {
        Clock clock = seqClock(BaseTime, BaseTime.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int tokenUser1 = singleUserSystemFacade.createSessionFor(User1, PasswordUser1);
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1),
                SystemFacade.sessionHasExpiredErrorDescription);
        assertThrowsLike( ()-> singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test33TokenSessionExpiresAndCeasesToExistAfterFiveMinutesWhenGettingBalance() {
        Clock clock = seqClock(BaseTime, BaseTime, BaseTime, BaseTime.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int tokenUser1 = singleUserSystemFacade.createSessionFor(User1, PasswordUser1);
        singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1);
        assertThrowsLike( () -> singleUserSystemFacade.getGiftCardBalance(GiftCardIdUser1, tokenUser1),
                SystemFacade.sessionHasExpiredErrorDescription);
        assertThrowsLike( ()-> singleUserSystemFacade.getGiftCardBalance(GiftCardIdUser1, tokenUser1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test34TokenSessionExpiresAndCeasesToExistAfterFiveMinutesWhenGettingTransactions() {
        Clock clock = seqClock(BaseTime, BaseTime, BaseTime, BaseTime.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int tokenUser1 = singleUserSystemFacade.createSessionFor(User1, PasswordUser1);
        singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1);
        assertThrowsLike( () -> singleUserSystemFacade.getTransactionsFor(GiftCardIdUser1, tokenUser1),
                SystemFacade.sessionHasExpiredErrorDescription);
        assertThrowsLike( ()-> singleUserSystemFacade.getTransactionsFor(GiftCardIdUser1, tokenUser1),
                SystemFacade.invalidTokenErrorDescription);
    }

    @Test
    public void test35TokenSessionDoesNotExpireWhenNotInactive() {
        //el plusMinutes(2) renueva el contador de la sesion, a los 6 minutos desde la creación no vence porque hubo actividad a los dos minutos.
        Clock clock = seqClock(BaseTime, BaseTime, BaseTime.plusMinutes(2), BaseTime.plusMinutes(6), BaseTime.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int tokenUser1 = singleUserSystemFacade.createSessionFor(User1, PasswordUser1);
        singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1);
        assertDoesNotThrow( () -> singleUserSystemFacade.getTransactionsFor(GiftCardIdUser1, tokenUser1));
    }

    @Test
    public void test36UserCanCreateAnotherSessionAfterCurrentOneExpires(){
        Clock clock = seqClock(BaseTime, BaseTime.plusMinutes(6), BaseTime.plusMinutes(6), BaseTime.plusMinutes(6), BaseTime.plusMinutes(6));

        singleUserSystemFacade = singleUserSystemFacade(clock);
        int tokenUser1 = singleUserSystemFacade.createSessionFor(User1, PasswordUser1);
        assertThrowsLike( () -> singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1),
                SystemFacade.sessionHasExpiredErrorDescription);
        assertThrowsLike( ()-> singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1),
                SystemFacade.invalidTokenErrorDescription);

        int secondTokenUser1 = singleUserSystemFacade.createSessionFor(User1, PasswordUser1);
        assertDoesNotThrow( ()-> singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, secondTokenUser1));
    }

    private void assertThrowsLike(Executable executable, String message) {assertEquals(message, assertThrows(Exception.class, executable).getMessage());}

    private int loginAndClaimSingleUser() {
        int tokenUser1 = loginSingleUser();
        singleUserSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1);
        return tokenUser1;
    }

    private int loginAndClaimMultiUser1() {
        int tokenUser1 = loginMultiUser1();
        multiUserAndGiftCardSystemFacade.claimGiftCard(GiftCardIdUser1, tokenUser1);
        return tokenUser1;
    }

    private int loginAndClaimMultiUser2() {
        int tokenUser2 = loginMultiUser2();
        multiUserAndGiftCardSystemFacade.claimGiftCard(GiftCardIdUser2, tokenUser2);
        return tokenUser2;
    }

    private int loginSingleUser() {return singleUserSystemFacade.createSessionFor(User1, PasswordUser1);}
    private void chargeSingleUser(int amount, String merchantKey) {singleUserSystemFacade.chargeGiftCard(amount, GiftCardIdUser1, merchantKey, User1);}
    private int getBalanceSingleUser(int token) {return singleUserSystemFacade.getGiftCardBalance(GiftCardIdUser1, token);}
    private void assertBalanceSingleUserEquals(int token, int expected) {assertEquals(expected, getBalanceSingleUser(token));}
    private List<Transaction> getTransactionsSingleUser(int token) {return singleUserSystemFacade.getTransactionsFor(GiftCardIdUser1, token);}
    private Transaction getFirstTransactionSingleUser(int token) {return getTransactionsSingleUser(token).get(0);}
    private int loginMultiUser1() {return multiUserAndGiftCardSystemFacade.createSessionFor(User1, PasswordUser1);}
    private int loginMultiUser2() {return multiUserAndGiftCardSystemFacade.createSessionFor(User2, PasswordUser2);}
    private int loginAndClaimBothCardsSingleUser() {
        int token = loginMultiUser1();
        multiUserAndGiftCardSystemFacade.claimGiftCard(GiftCardIdUser1, token);
        multiUserAndGiftCardSystemFacade.claimGiftCard(GiftCardIdUser2, token);
        return token;
    }
    private void chargeMulti(int amount, Integer giftCardId, String merchantKey, String userName) {multiUserAndGiftCardSystemFacade.chargeGiftCard(amount, giftCardId, merchantKey, userName);}    
    private List<Transaction> getTransactionsMulti(int giftCardId, int token) {return multiUserAndGiftCardSystemFacade.getTransactionsFor(giftCardId, token);}
    private Transaction firstTransactionMulti(int giftCardId, int token) {return getTransactionsMulti(giftCardId, token).get(0);}

    private Clock seqClock(LocalDateTime... times) {
        return new Clock() {
            int i = 0;
            Iterator<LocalDateTime> seq = java.util.List.of(times).iterator();
            public LocalDateTime getTime() { i++; return seq.next(); }
        };
    }

    private int[] loginAndClaimBothUsers() {
        int tokenUser1 = loginAndClaimMultiUser1();
        int tokenUser2 = loginAndClaimMultiUser2();
        return new int[]{tokenUser1, tokenUser2};
    }

    private void chargeBothUsers(int amountUser1, int amountUser2) {
        chargeMulti(amountUser1, GiftCardIdUser1, ValidMerchantKey, User1);
        chargeMulti(amountUser2, GiftCardIdUser2, ValidMerchantKey2, User2);
    }

    private void assertTransactionsFirstAmounts(int tokenUser1, int tokenUser2, int amountUser1, int amountUser2) {
        assertEquals(amountUser1, firstTransactionMulti(GiftCardIdUser1, tokenUser1).getAmount());
        assertEquals(amountUser2, firstTransactionMulti(GiftCardIdUser2, tokenUser2).getAmount());
    }

    private void assertTransactionsFirstKeysDefault(int tokenUser1, int tokenUser2) {
        assertEquals(ValidMerchantKey, firstTransactionMulti(GiftCardIdUser1, tokenUser1).getMerchantKey());
        assertEquals(ValidMerchantKey2, firstTransactionMulti(GiftCardIdUser2, tokenUser2).getMerchantKey());
    }

    private void assertTransactionsFirstTimesDefault(int tokenUser1, int tokenUser2) {
        assertEquals(BaseTime, firstTransactionMulti(GiftCardIdUser1, tokenUser1).getTime());
        assertEquals(BaseTime, firstTransactionMulti(GiftCardIdUser2, tokenUser2).getTime());
    }

    private void assertTransactionsSizeBoth(int tokenUser1, int tokenUser2, int expectedTransactionSize) {
        assertEquals(expectedTransactionSize, getTransactionsMulti(GiftCardIdUser1, tokenUser1).size());
        assertEquals(expectedTransactionSize, getTransactionsMulti(GiftCardIdUser2, tokenUser2).size());
    }
}
