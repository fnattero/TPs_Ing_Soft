package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class UserSessionTest {

    private static final int TOKEN = 1;
    private static final String USERNAME = "user";
    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Test
    public void test01NotExpiredWhenNowIsBeforeExpireTime() {
        Clock expire = new Clock(BASE.plusMinutes(5));
        Clock now = new Clock(BASE);
        UserSession session = new UserSession(TOKEN, USERNAME, expire);
        assertFalse(session.isExpired(now));
    }

    @Test
    public void test02ExpiredWhenNowIsAfterExpireTime() {
        Clock expire = new Clock(BASE.minusMinutes(1));
        Clock now = new Clock(BASE);
        UserSession session = new UserSession(TOKEN, USERNAME, expire);
        assertTrue(session.isExpired(now));
    }

    @Test
    public void test03NotExpiredWhenNowEqualsExpireTime() {
        Clock expire = new Clock(BASE);
        Clock now = new Clock(BASE);
        UserSession session = new UserSession(TOKEN, USERNAME, expire);
        assertFalse(session.isExpired(now));
    }
}
