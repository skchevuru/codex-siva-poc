package com.example.statement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatementParserTest {

    @Test
    void parseTextExtractsTransactions() {
        String sample = """
                01/15 COFFEE SHOP DOWNTOWN 5.99 DR
                01/16 PAYMENT RECEIVED -100.00
                01/17 GROCERY STORE 45.12
                """;

        StatementParser parser = new StatementParser();
        List<TransactionRecord> transactions = parser.parseText(sample);

        assertEquals(3, transactions.size());
        assertEquals(TransactionRecord.TransactionType.DEBIT, transactions.get(0).transactionType());
        assertEquals(TransactionRecord.TransactionType.CREDIT, transactions.get(1).transactionType());
        assertEquals("PAYMENT RECEIVED", transactions.get(1).description());
    }
}
