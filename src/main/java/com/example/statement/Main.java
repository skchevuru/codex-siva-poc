package com.example.statement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java -jar credit-card-statement-parser.jar <input.pdf> <output.csv>");
            System.exit(1);
        }

        Path inputPdf = Path.of(args[0]);
        Path outputCsv = Path.of(args[1]);

        if (!Files.exists(inputPdf)) {
            throw new IllegalArgumentException("Input PDF not found: " + inputPdf);
        }

        StatementParser parser = new StatementParser();
        List<TransactionRecord> transactions = parser.parse(inputPdf);

        writeCsv(transactions, outputCsv);

        System.out.println("Parsed transactions: " + transactions.size());
        System.out.println("CSV written to: " + outputCsv.toAbsolutePath());
    }

    private static void writeCsv(List<TransactionRecord> transactions, Path outputCsv) throws IOException {
        Files.createDirectories(outputCsv.toAbsolutePath().getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(outputCsv)) {
            writer.write("date,amount,transaction_description,credit_debit,source_line");
            writer.newLine();
            for (TransactionRecord transaction : transactions) {
                writer.write(csv(transaction.date()));
                writer.write(',');
                writer.write(transaction.amount().toPlainString());
                writer.write(',');
                writer.write(csv(transaction.description()));
                writer.write(',');
                writer.write(transaction.transactionType().name());
                writer.write(',');
                writer.write(csv(transaction.sourceLine()));
                writer.newLine();
            }
        }
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
