package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class SystemFacadeTest {

    private static final String VALID_USER = "Juan";
    private static final String VALID_PASSWORD = "Contra_de_juan";
    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    SystemFacade systemFacade;

    @BeforeEach
    public void beforeEach() {
        Map<String, String> users = new HashMap<>();
        users.put(VALID_USER, VALID_PASSWORD);
        Map<String, Integer> tokens = new HashMap<>();
        List<String> merchantKeys = new ArrayList<>();
        Clock now = new Clock(BASE);
        systemFacade = new SystemFacade(users, tokens, merchantKeys, now);
    }

    @Test
    public void test01CanInstantiateSystemFacade() {
        assertNotNull(systemFacade);
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message,
                assertThrows(Exception.class, executable)
                        .getMessage());
    }
}
