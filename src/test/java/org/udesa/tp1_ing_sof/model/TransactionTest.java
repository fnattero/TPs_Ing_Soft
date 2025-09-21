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
        Transaction transaction = newTransaction(VALID_AMOUNT, validTime);
        assertTransactionFields(transaction, VALID_AMOUNT, validTime);
    }

    @Test
    void test02ValidTransactionWithPastTime() {
        Transaction transaction = newTransaction(VALID_AMOUNT, pastTime);
        assertTransactionFields(transaction, VALID_AMOUNT, pastTime);
    }

    @Test
    void test03ValidTransactionWithZeroAmount() {
        Transaction transaction = newTransaction(ZERO_AMOUNT, validTime);
        assertTransactionFields(transaction, ZERO_AMOUNT, validTime);
    }

    @Test
    void test04CurrentTimeIsValid() {
        LocalDateTime almostNow = LocalDateTime.now().minusSeconds(1);
        assertDoesNotThrow(() -> newTransaction(VALID_AMOUNT, almostNow));
    }

    @Test
    void test05LargeAmountValue() {
        Transaction transaction = newTransaction(MAX_AMOUNT, validTime);
        assertTransactionFields(transaction, MAX_AMOUNT, validTime);
    }

    @Test
    void test09MerchantKeyIsCorrect(){
        Transaction transaction = newTransaction(MAX_AMOUNT, validTime);
        assertEquals(VALID_MERCHANT_KEY, transaction.getMerchantKey());
    }

    @Test
    void test06NegativeAmountThrowsException() {
        assertThrowsLike(() -> newTransaction(NEGATIVE_AMOUNT, validTime), Transaction.negativeAmountErrorDescription);
    }

    @Test
    void test08FutureTimeThrowsException() {
        assertThrowsLike(() -> newTransaction(VALID_AMOUNT, futureTime), Transaction.futureTimeErrorDescription);
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }

    private Transaction newTransaction(int amount, LocalDateTime time) {
        return new Transaction(amount, VALID_MERCHANT_KEY, time);
    }

    private void assertTransactionFields(Transaction transaction, int expectedAmount, LocalDateTime expectedTime) {
        assertEquals(expectedAmount, transaction.getAmount());
        assertEquals(expectedTime, transaction.getTime());
    }
}
