package org.udesa.tp1_ing_sof.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class SystemFacadeTest {

    SystemFacade systemFacade;
    public String validUserName = "Juan";
    public String validPassword = "Contra_de_juan";

    @BeforeEach
    public void  beforeEach() {
        systemFacade = new systemFacade();
    }




    private void assertThrowsLike(Executable executable, String message ) {
        assertEquals( message,
                assertThrows( Exception.class, executable )
                        .getMessage() );
    }
}
