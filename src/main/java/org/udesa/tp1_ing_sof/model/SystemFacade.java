package org.udesa.tp1_ing_sof.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemFacade {

    public static String sessionHasExpiredErrorDescription = "Can not use the session after minutes of inactivity";
    public static String invalidUserAndOrPasswordErrorDescription = "Invalid user and/or password";
    public static String invalidTokenErrorDescription = "Invalid token";

    private final Map<String, String> validUsers;
    private final List<String> merchantKeys;
    private final Clock clock;
    private final Map<Integer, GiftCard> giftCards;
    private Map<Integer, TokenSession> tokenSessions = new HashMap<>();
    private int id = 0;

    public SystemFacade(Map<String, String> validUsers, List<String> merchantKeys, Map<Integer, GiftCard> giftCards, Clock clock) {
        this.validUsers = validUsers;
        this.clock = clock;
        this.merchantKeys = merchantKeys;
        this.giftCards = giftCards;
    }

    public int createSessionFor( String userName, String pass ) {
        checkValidUser( userName, pass );
        int token = id++;
        tokenSessions.put( token, new TokenSession(clock));
        return token;
    }

    private TokenSession tokenSessionIdentifiedAs( int token ) {
        TokenSession tokenSession = tokenSessions.computeIfAbsent( token, id1 -> { throw new RuntimeException( invalidTokenErrorDescription );} );
        checkTokenSessionIsActive( token, tokenSession );
        return tokenSession;
    }

    private void checkTokenSessionIsActive( int token, TokenSession tokenSession ) {
        if ( tokenSession.isExpired() ) {
            tokenSessions.remove( token );
            throw new RuntimeException( sessionHasExpiredErrorDescription );
        }
    }

    public

    private void checkValidUser(String userName, String pass) {
        if ( !pass.equals( validUsers.get( userName ) ) ) {
            throw new RuntimeException( invalidUserAndOrPasswordErrorDescription );
        }
    }


    public boolean sessionExists(int token) {
        return tokenSessions.containsKey(token) && tokenSessions.get(token).isExpired(this.clock.getTime());
    }
}
