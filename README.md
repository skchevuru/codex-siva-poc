# Credit Card Statement PDF Parser (Java)

This project provides a Java CLI program that reads a credit-card PDF statement and extracts transactions into CSV with these fields:

- `date`
- `amount`
- `transaction_description`
- `credit_debit`
- `source_line` (useful for debugging parser misses)

## How it works

1. Uses **Apache PDFBox** to extract text from the PDF.
2. Applies a transaction regex + fallback line parser.
3. Infers credit/debit from explicit markers (`CR/DR`, `CREDIT/DEBIT`), amount sign, and keywords (`payment/refund/reversal`).
4. Exports normalized rows to CSV.

## Build

```bash
mvn clean package
```

Produces a runnable fat jar:

- `target/credit-card-statement-parser-1.0.0-jar-with-dependencies.jar`

## Run

```bash
java -jar target/credit-card-statement-parser-1.0.0-jar-with-dependencies.jar \
  /path/to/statement.pdf \
  ./output/transactions.csv
```

## Notes and limitations

- Statement layouts vary by bank; you may need to tune regex logic in `StatementParser`.
- If the PDF is image-only (scanned), text extraction may fail without OCR.

## APIs / Services that can do statement extraction

If you want higher accuracy and less custom parsing, these APIs are commonly used:

1. **AWS Textract** + custom post-processing
   - OCR + table/key-value extraction.
2. **Google Cloud Document AI** (Form Parser / specialized processors)
   - Strong document parsing with structured outputs.
3. **Azure AI Document Intelligence** (formerly Form Recognizer)
   - Extracts tables, fields, and supports custom models.
4. **Veryfi API**
   - Purpose-built financial docs/receipts/invoices extraction.
5. **Mindee API**
   - Prebuilt/custom document parsing pipelines.
6. **Nanonets OCR API**
   - Trainable document extraction workflows.

Open-source/local options:

- **Apache PDFBox** (used here) for embedded text PDFs.
- **Tesseract OCR** for scanned image PDFs.
- **Tabula/Camelot equivalents** for table-style statements (often requires layout tuning).

## Suggested production approach

- Start with this local parser for low-cost baseline processing.
- Add OCR fallback for scanned PDFs.
- For multi-bank high-accuracy extraction, use a managed document AI API with validation rules.
