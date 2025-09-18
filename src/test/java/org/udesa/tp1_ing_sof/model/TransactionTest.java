package org.udesa.tp1_ing_sof.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
    void testNegativeAmountThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(NEGATIVE_AMOUNT, validTime)
        );
        assertEquals(AMOUNT_NON_NEGATIVE_ERROR, exception.getMessage());
    }

    @Test
    void testNullTimeThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(VALID_AMOUNT, null)
        );
        assertEquals(TIME_NULL_ERROR, exception.getMessage());
    }

    @Test
    void testFutureTimeThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(VALID_AMOUNT, futureTime)
        );
        assertEquals(TIME_FUTURE_ERROR, exception.getMessage());
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
    void testMultipleValidationErrors() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(NEGATIVE_AMOUNT, null)
        );
        assertEquals(AMOUNT_NON_NEGATIVE_ERROR, exception.getMessage());
    }
}