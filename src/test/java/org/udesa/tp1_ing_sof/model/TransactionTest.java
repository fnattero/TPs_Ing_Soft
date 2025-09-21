package org.udesa.tp1_ing_sof.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class TransactionTest {

    private static final int ValidAmount = 100;
    private static final int ZeroAmount = 0;
    private static final int MaxAmount = Integer.MAX_VALUE;
    private static final String ValidMerchantKey = "MercadoPagoKey";

    private LocalDateTime validTime;
    private LocalDateTime pastTime;

    @BeforeEach
    void setUp() {
        validTime = LocalDateTime.now().minusMinutes(1);
        pastTime = LocalDateTime.now().minusDays(1);
    }

    @Test
    void test01ValidTransaction() {
        Transaction transaction = newTransaction(ValidAmount, validTime);
        assertTransactionFields(transaction, ValidAmount, validTime);
    }

    @Test
    void test02ValidTransactionWithPastTime() {
        Transaction transaction = newTransaction(ValidAmount, pastTime);
        assertTransactionFields(transaction, ValidAmount, pastTime);
    }

    @Test
    void test03ValidTransactionWithZeroAmount() {
        Transaction transaction = newTransaction(ZeroAmount, validTime);
        assertTransactionFields(transaction, ZeroAmount, validTime);
    }

    @Test
    void test04CurrentTimeIsValid() {
        LocalDateTime almostNow = LocalDateTime.now().minusSeconds(1);
        assertDoesNotThrow(() -> newTransaction(ValidAmount, almostNow));
    }

    @Test
    void test05LargeAmountValue() {
        Transaction transaction = newTransaction(MaxAmount, validTime);
        assertTransactionFields(transaction, MaxAmount, validTime);
    }

    @Test
    void test09MerchantKeyIsCorrect(){
        Transaction transaction = newTransaction(MaxAmount, validTime);
        assertEquals(ValidMerchantKey, transaction.getMerchantKey());
    }

    private Transaction newTransaction(int amount, LocalDateTime time) {
        return new Transaction(amount, ValidMerchantKey, time);
    }

    private void assertTransactionFields(Transaction transaction, int expectedAmount, LocalDateTime expectedTime) {
        assertEquals(expectedAmount, transaction.getAmount());
        assertEquals(expectedTime, transaction.getTime());
    }
}
