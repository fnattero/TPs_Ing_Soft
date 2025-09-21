package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

public class TokenSessionTest {

    private static final String User1 = "Juan";
    private static final LocalDateTime BaseTime = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Test
    public void test01NotExpiredWhenNowIsBeforeExpireTime() {
        Clock clock = seqClock(BaseTime, BaseTime);
        TokenSession session = new TokenSession(User1, clock);
        assertFalse(session.isExpired());
    }

    @Test
    public void test02ExpiredWhenNowIsAfterExpireTime() {
        Clock clock = seqClock(BaseTime, BaseTime, BaseTime.plusHours(1));

        TokenSession session = new TokenSession(User1, clock);
        assertFalse(session.isExpired());
    }

    private Clock seqClock(LocalDateTime... times) {
        return new Clock() {
            Iterator<LocalDateTime> seq = java.util.List.of(times).iterator();
            public LocalDateTime getTime() { return seq.next(); }
        };
    }
}
