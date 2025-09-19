package org.udesa.tp1_ing_sof.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class TransactionTest {

    private static final int VALID_AMOUNT = 100;
    private static final int ZERO_AMOUNT = 0;
    private static final int NEGATIVE_AMOUNT = -1;
    private static final int MAX_AMOUNT = Integer.MAX_VALUE;
    
    private static final String AMOUNT_NON_NEGATIVE_ERROR = "amount must be non-negative";
    private static final String TIME_NULL_ERROR = "time cannot be null";
    private static final String TIME_FUTURE_ERROR = "time cannot be in the future";

    private LocalDateTime validTime;
    private LocalDateTime pastTime;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() {
        validTime = LocalDateTime.now().minusMinutes(1);
        pastTime = LocalDateTime.now().minusDays(1);
        futureTime = LocalDateTime.now().plusMinutes(1);
    }

    @Test
    void testValidTransaction() {
        Transaction transaction = new Transaction(VALID_AMOUNT, validTime);
        
        assertEquals(VALID_AMOUNT, transaction.getAmount());
        assertEquals(validTime, transaction.getTime());
    }

    @Test
    void testValidTransactionWithPastTime() {
        Transaction transaction = new Transaction(VALID_AMOUNT, pastTime);
        
        assertEquals(VALID_AMOUNT, transaction.getAmount());
        assertEquals(pastTime, transaction.getTime());
    }

    @Test
    void testValidTransactionWithZeroAmount() {
        Transaction transaction = new Transaction(ZERO_AMOUNT, validTime);
        
        assertEquals(ZERO_AMOUNT, transaction.getAmount());
        assertEquals(validTime, transaction.getTime());
    }

    @Test
    void testCurrentTimeIsValid() {
        LocalDateTime almostNow = LocalDateTime.now().minusSeconds(1);
        assertDoesNotThrow(() -> new Transaction(VALID_AMOUNT, almostNow));
    }

    @Test
    void testLargeAmountValue() {
        Transaction transaction = new Transaction(MAX_AMOUNT, validTime);
        assertEquals(MAX_AMOUNT, transaction.getAmount());
    }

    @Test
    void testNegativeAmountThrowsException() {
        assertThrowsLike(() -> new Transaction(NEGATIVE_AMOUNT, validTime),AMOUNT_NON_NEGATIVE_ERROR);
    }

    @Test
    void testNullTimeThrowsException() {
        assertThrowsLike(() -> new Transaction(VALID_AMOUNT, null),TIME_NULL_ERROR);
    }

    @Test
    void testFutureTimeThrowsException() {
        assertThrowsLike(() -> new Transaction(VALID_AMOUNT, futureTime),TIME_FUTURE_ERROR);
    }

    @Test
    void testMultipleValidationErrors() {
        assertThrowsLike(() -> new Transaction(NEGATIVE_AMOUNT, null),AMOUNT_NON_NEGATIVE_ERROR);
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }
}
