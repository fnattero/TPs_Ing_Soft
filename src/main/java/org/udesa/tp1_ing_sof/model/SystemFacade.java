package org.udesa.tp1_ing_sof.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemFacade {

    public static String sessionHasExpiredErrorDescription = "Can not use the session after minutes of inactivity";
    public static String invalidUserAndOrPasswordErrorDescription = "Invalid user and/or password";
    public static String invalidTokenErrorDescription = "Invalid token";
    public static String invalidGiftCardIDErrorDescription =  "Invalid gift card ID";
    public static String invalidMerchantKeyErrorDescription = "Invalid merchant key";
    public static String invalidMerchantChargeAmountErrorDescription =  "Invalid merchant charge amount";

    private final Map<String, String> validUsers;
    private final Set<String> merchantKeys; //que sea un set
    private final Clock clock;
    private final Map<Integer, GiftCard> giftCards;
    private Map<Integer, TokenSession> tokenSessions = new HashMap<>();
    private int id = 0;

    public SystemFacade(Map<String, String> validUsers, Set<String> merchantKeys, Map<Integer, GiftCard> giftCards, Clock clock) {
        this.validUsers = validUsers;
        this.clock = clock;
        this.merchantKeys = merchantKeys;
        this.giftCards = giftCards;
    }

    public int createSessionFor( String userName, String pass ) {
        checkValidUser( userName, pass );
        int token = id++;
        tokenSessions.put( token, new TokenSession(userName, clock));
        return token;
    }

    private TokenSession tokenSessionIdentifiedAs( int token ) {
        TokenSession tokenSession = tokenSessions.computeIfAbsent( token, id1 -> { throw new RuntimeException( invalidTokenErrorDescription );} );
        checkTokenSessionIsActive( token );
        return tokenSession;
    }

    private void checkTokenSessionIsActive( int token ) {

        if ( tokenSessions.get(token).isExpired() ) {
            tokenSessions.remove( token );
            throw new RuntimeException( sessionHasExpiredErrorDescription );
        }
    }

    private void checkValidUser(String userName, String pass) {
        if ( !pass.equals( validUsers.get( userName ) ) ) {
            throw new RuntimeException( invalidUserAndOrPasswordErrorDescription );
        }
    }

    public boolean sessionExists(int token) {
        return tokenSessions.containsKey(token) && !tokenSessions.get(token).isExpired();
    }

    public void claimGiftCard(Integer giftCardId, int token) {
        checkTokenSessionIsActive(token);
        giftCards.get(giftCardId).claimCard(tokenSessions.get(token).getUsername());
    }

    public int checkBalance(Integer giftCardId, int token) {

        checkTokenSessionIsActive(token);
        checkValidGiftCard(giftCardId);
        return giftCards.get(giftCardId).getBalance();
    }

    private void checkValidGiftCard(Integer giftCardId) {
        if ( !giftCards.containsKey(giftCardId) ) {
            throw new RuntimeException( invalidGiftCardIDErrorDescription );
        }
    }


    public void merchantCharge(int amount, Integer giftCardId, String merchantKey) {
        checkValidMerchant(merchantKey);
        checkValidGiftCard(giftCardId);
        CheckValidChargeAmount(amount);
        giftCards.get(giftCardId).charge(amount, clock.getTime());
    }

    private void CheckValidChargeAmount(int amount) {
        if (amount < 0) {
            throw new RuntimeException( invalidMerchantChargeAmountErrorDescription );
        }
    }

    private void checkValidMerchant(String merchantKey) {
        if ( !merchantKeys.contains( merchantKey ) ) {
            throw new RuntimeException(  );
        }
    }
}
