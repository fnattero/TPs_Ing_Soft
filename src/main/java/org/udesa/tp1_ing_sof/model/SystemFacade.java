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

    private final Map<String, String> validUsers;
    private final Set<String> merchantKeys; //que sea un set
    private final Clock clock;
    private final Map<Integer, GiftCard> giftCards;
    private Map<Integer, TokenSession> tokenSessions = new HashMap<>();
    private int next_token_id = 0;

    public SystemFacade(Map<String, String> validUsers, Set<String> merchantKeys, Map<Integer, GiftCard> giftCards, Clock clock) {
        this.validUsers = validUsers;
        this.clock = clock;
        this.merchantKeys = merchantKeys;
        this.giftCards = giftCards;
    }

    public int createSessionFor( String userName, String pass ) {
        checkValidUser( userName );
        checkValidPassword(userName, pass);
        int token = next_token_id++;
        tokenSessions.put( token, new TokenSession(userName, clock));
        return token;
    }

    public void claimGiftCard(int giftCardId, int token) {
        checkTokenSessionExists(token);
        checkTokenSessionIsActive(token);
        checkValidGiftCard(giftCardId);
        tokenSessions.get(token).updateLastAccess();
        giftCards.get(giftCardId).claimCard(tokenSessions.get(token).getUsername());
    }

    public int getGiftCardBalance(int giftCardId, int token) {
        checkTokenSessionExists(token);
        checkTokenSessionIsActive(token);
        checkValidGiftCard(giftCardId);
        tokenSessions.get(token).updateLastAccess();
        return giftCards.get(giftCardId).getBalance(tokenSessions.get(token).getUsername());
    }

    public void chargeGiftCard(int amount, Integer giftCardId, String merchantKey, String userName) {
        checkValidMerchant(merchantKey);
        checkValidGiftCard(giftCardId);
        checkValidUser(userName);
        giftCards.get(giftCardId).charge(amount, merchantKey, userName, clock);
    }

    public List<Transaction> getTransactionsFor(Integer giftCardId, int token) {
        checkTokenSessionExists(token);
        checkTokenSessionIsActive(token);
        checkValidGiftCard(giftCardId);
        tokenSessions.get(token).updateLastAccess();
        return giftCards.get(giftCardId).getTransactions(tokenSessions.get(token).getUsername());
    }

    private void checkTokenSessionExists(int token) {
        if (!tokenSessions.containsKey(token)){
            throw new RuntimeException(invalidTokenErrorDescription);
        }
    }

    private void checkTokenSessionIsActive( int token ) {
        if ( tokenSessions.get(token).isExpired() ) {
            tokenSessions.remove( token );
            throw new RuntimeException( sessionHasExpiredErrorDescription );
        }
    }

    private void checkValidUser(String userName) {
        if (!validUsers.containsKey(userName)) {
            throw new RuntimeException(invalidUserAndOrPasswordErrorDescription);
        }
    }
    private void checkValidPassword(String userName, String pass) {
        if ( !pass.equals( validUsers.get( userName ) ) ) {
            throw new RuntimeException( invalidUserAndOrPasswordErrorDescription );
        }
    }

    private void checkValidGiftCard(Integer giftCardId) {
        if ( !giftCards.containsKey(giftCardId) ) {
            throw new RuntimeException( invalidGiftCardIDErrorDescription );
        }
    }

    private void checkValidMerchant(String merchantKey) {
        if ( !merchantKeys.contains( merchantKey ) ) {
            throw new RuntimeException( invalidMerchantKeyErrorDescription );
        }
    }

}
