package org.udesa.tp1_ing_sof.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UserTest {

    private static final String USERNAME = "Juan";
    private static final String PASSWORD = "Contra_de_juan";
    private static final int USER_ID = 0;

    @Test
    public void test01UserStoresAndReturnsFields() {
        User user = new User(USERNAME, PASSWORD, USER_ID);
        assertEquals(USERNAME, user.getUserName());
        assertEquals(PASSWORD, user.getPassword());
        assertEquals(USER_ID, user.getUserId());
    }
}
