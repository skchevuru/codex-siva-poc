package com.example.statement;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatementParser {

    private static final Pattern TRANSACTION_LINE_PATTERN = Pattern.compile(
            "^(?<date>\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?)\\s+(?<description>.+?)\\s+(?<amount>[+\\-]?(?:USD\\s*)?\\$?\\d[\\d,]*\\.\\d{2})\\s*(?<type>CR|DR|CREDIT|DEBIT)?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("[+\\-]?(?:USD\\s*)?\\$?\\d[\\d,]*\\.\\d{2}");

    public List<TransactionRecord> parse(Path pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return parseText(text);
        }
    }

    List<TransactionRecord> parseText(String text) {
        List<TransactionRecord> results = new ArrayList<>();
        for (String rawLine : text.split("\\R")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                continue;
            }

            Matcher matcher = TRANSACTION_LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                String date = matcher.group("date");
                String description = matcher.group("description");
                String amountText = matcher.group("amount");
                String typeText = matcher.group("type");

                BigDecimal amount = parseAmount(amountText);
                TransactionRecord.TransactionType type = inferType(amount, typeText, description);

                results.add(new TransactionRecord(date, description, amount.abs(), type, line));
                continue;
            }

            if (looksLikeFallbackTransactionLine(line)) {
                TransactionRecord fallbackRecord = parseFallbackLine(line);
                if (fallbackRecord != null) {
                    results.add(fallbackRecord);
                }
            }
        }

        return results;
    }

    private boolean looksLikeFallbackTransactionLine(String line) {
        return line.matches("^\\d{1,2}[/-]\\d{1,2}.*") && AMOUNT_PATTERN.matcher(line).find();
    }

    private TransactionRecord parseFallbackLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 3) {
            return null;
        }

        String date = parts[0];
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(line);
        if (!amountMatcher.find()) {
            return null;
        }

        String amountToken = amountMatcher.group();
        BigDecimal amount = parseAmount(amountToken);

        int amountStart = line.indexOf(amountToken);
        String description = line.substring(date.length(), amountStart).trim();
        if (description.isBlank()) {
            description = "UNKNOWN";
        }

        TransactionRecord.TransactionType type = inferType(amount, null, description);

        return new TransactionRecord(date, description, amount.abs(), type, line);
    }

    private BigDecimal parseAmount(String amountText) {
        String normalized = amountText
                .replace("USD", "")
                .replace("$", "")
                .replace(",", "")
                .trim();
        return new BigDecimal(normalized);
    }

    private TransactionRecord.TransactionType inferType(BigDecimal amount, String typeText, String description) {
        if (typeText != null) {
            String normalizedType = typeText.toUpperCase(Locale.ROOT);
            if (normalizedType.equals("CR") || normalizedType.equals("CREDIT")) {
                return TransactionRecord.TransactionType.CREDIT;
            }
            if (normalizedType.equals("DR") || normalizedType.equals("DEBIT")) {
                return TransactionRecord.TransactionType.DEBIT;
            }
        }

        if (amount.signum() < 0) {
            return TransactionRecord.TransactionType.CREDIT;
        }
        if (amount.signum() > 0) {
            return TransactionRecord.TransactionType.DEBIT;
        }

        String lowerDescription = description.toLowerCase(Locale.ROOT);
        if (lowerDescription.contains("payment") || lowerDescription.contains("refund") || lowerDescription.contains("reversal")) {
            return TransactionRecord.TransactionType.CREDIT;
        }

        return TransactionRecord.TransactionType.UNKNOWN;
    }
}
