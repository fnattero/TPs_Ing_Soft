package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ClockTest {

    private static final LocalDateTime T = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Test
    public void test01ClockReturnsProvidedTime() {
        Clock clock = new Clock(T);
        assertEquals(T, clock.getTime());
    }
}
