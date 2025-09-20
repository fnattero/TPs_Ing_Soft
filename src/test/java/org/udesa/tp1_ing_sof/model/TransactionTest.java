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
    private static final String VALID_MERCHANT_KEY = "MercadoPago_key";

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
    void test01ValidTransaction() {
        Transaction transaction = new Transaction(VALID_AMOUNT, VALID_MERCHANT_KEY,validTime);
        
        assertEquals(VALID_AMOUNT, transaction.getAmount());
        assertEquals(validTime, transaction.getTime());
    }

    @Test
    void test02ValidTransactionWithPastTime() {
        Transaction transaction = new Transaction(VALID_AMOUNT, VALID_MERCHANT_KEY, pastTime);
        
        assertEquals(VALID_AMOUNT, transaction.getAmount());
        assertEquals(pastTime, transaction.getTime());
    }

    @Test
    void test03ValidTransactionWithZeroAmount() {
        Transaction transaction = new Transaction(ZERO_AMOUNT, VALID_MERCHANT_KEY, validTime);
        
        assertEquals(ZERO_AMOUNT, transaction.getAmount());
        assertEquals(validTime, transaction.getTime());
    }

    @Test
    void test04CurrentTimeIsValid() {
        LocalDateTime almostNow = LocalDateTime.now().minusSeconds(1);
        assertDoesNotThrow(() -> new Transaction(VALID_AMOUNT, VALID_MERCHANT_KEY, almostNow));
    }

    @Test
    void test05LargeAmountValue() {
        Transaction transaction = new Transaction(MAX_AMOUNT, VALID_MERCHANT_KEY, validTime);
        assertEquals(MAX_AMOUNT, transaction.getAmount());
    }

    @Test
    void test06NegativeAmountThrowsException() {
        assertThrowsLike(() -> new Transaction(NEGATIVE_AMOUNT, VALID_MERCHANT_KEY, validTime), Transaction.negativeAmountErrorDescription);
    }

    @Test
    void test08FutureTimeThrowsException() {
        assertThrowsLike(() -> new Transaction(VALID_AMOUNT, VALID_MERCHANT_KEY, futureTime),Transaction.futureTimeErrorDescription);
    }

    @Test
    void test09MerchantKeyIsCorrect(){
        Transaction transaction = new Transaction(MAX_AMOUNT, VALID_MERCHANT_KEY, validTime);
        assertEquals(VALID_MERCHANT_KEY, transaction.getMerchantKey());
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }
}
