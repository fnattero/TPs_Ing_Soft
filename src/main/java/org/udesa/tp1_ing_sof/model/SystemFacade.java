package org.udesa.tp1_ing_sof.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemFacade {

    public static String sessionHasExpiredErrorDescription = "Can not use the cart after minutes of inactivity";
    public static String invalidUserAndOrPasswordErrorDescription = "Invalid user and/or password";
    public static String invalidCartIdErrorDescription = "Invalid cart id";

    private final Map<String, String> validUsers;
    private final List<String> merchantKeys;
    private final Map<String, Integer> tokens;
    private final Clock clock;
    private int id = 0;
    private Map<String, UserSession> UserSessions = new HashMap();

    public SystemFacade(Map<String, String> validUsers, Map<String, Integer> tokens, List<String> merchantKeys, Clock clock) {
        this.validUsers = validUsers;
        this.tokens = tokens;
        this.clock = clock;
        this.merchantKeys = merchantKeys;
    }

    public void createSessionFor( String userName, String pass ) {
        checkValidUser( userName, pass );
        createUserToken(userName);

        UserSessions.put( userName, new UserSession( tokens.get(userName),
                userName,
                new Clock(clock.getTime().plus(java.time.Duration.ofMinutes(5)))));
    }

    private void createUserToken(String userName) {

    }

    private void checkValidUser(String userName, String pass) {
        if ( !pass.equals( validUsers.get( userName ) ) ) {
            throw new RuntimeException( invalidUserAndOrPasswordErrorDescription );
        }
    }




}
