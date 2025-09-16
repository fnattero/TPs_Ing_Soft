package org.udesa.tp1_ing_sof.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    public String validUserName = "Juan";
    public String validPassword = "Contra_de_juan";

    @Test
    public void test01UserNameIsCorrect() {
        User user = new User(validUserName, validPassword, 0);
        assertEquals(user.getUserName(), validUserName);
        assertEquals(user.getPassword(), validPassword);
    }


}
