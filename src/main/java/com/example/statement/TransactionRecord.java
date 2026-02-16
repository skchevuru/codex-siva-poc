package com.example.statement;

import java.math.BigDecimal;

public record TransactionRecord(
        String date,
        String description,
        BigDecimal amount,
        TransactionType transactionType,
        String sourceLine
) {
    public enum TransactionType {
        CREDIT,
        DEBIT,
        UNKNOWN
    }
}
