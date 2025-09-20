package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TokenSessionTest {

    private static final String USERNAME = "user";
    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Test
    public void test01NotExpiredWhenNowIsBeforeExpireTime() {
        TokenSession session = new TokenSession(USERNAME, new Clock(BASE));
        assertFalse(session.isExpired());
    }

    @Test
    public void test02ExpiredWhenNowIsAfterExpireTime() {
        Clock myClock = new Clock(BASE) {
            Iterator<LocalDateTime> seq = java.util.List.of(
                    BASE,
                    BASE,
                    BASE.plusMinutes(5)
            ).iterator();
            public LocalDateTime getTime() {
                return seq.next();
            }
        };

        TokenSession session = new TokenSession(USERNAME, myClock);
        assertFalse(session.isExpired());
    }
    }
