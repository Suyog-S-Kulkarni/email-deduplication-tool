# Email Deduplication Tool

A Java and Spring Boot application that processes an Excel workbook,
normalizes email addresses, and generates separate Excel reports for
unique and duplicate occurrences.

## Features

- Upload an XLSX file through a simple web interface.
- Validate the Email column and reject invalid input.
- Normalize surrounding whitespace and letter case.
- Identify duplicate occurrences using a HashSet.
- Preserve encounter order and original Excel row numbers.
- Download unique and duplicate email reports separately.
- Display processed, unique and duplicate counts.

## Technology Stack

- Java 21
- Spring Boot
- Spring MVC REST API
- Jakarta Validation
- Apache POI
- Java Collections
- HTML, CSS and JavaScript
- JSZip
- Maven
- JUnit

## How It Works

1. Select an Excel file and click Check.
2. The browser uploads it to the REST API.
3. The backend validates and reads the first worksheet.
4. Email addresses are normalized and classified.
5. Two Excel reports are generated and returned in a ZIP archive.
6. The browser enables separate download buttons for both reports.

## Input Requirements

- File format: .xlsx
- Maximum file size: 5 MiB
- First worksheet must contain an Email header in its first row.
- Email values must be text cells.
- Blank email cells are skipped.
- Invalid email entries cause the request to be rejected.

## Duplicate Counting Rules

The first occurrence of each normalized address goes into the unique
report. Every subsequent occurrence goes into the duplicate report.

Processed emails = Unique emails + Duplicate occurrences

For example, [a@example.com, b@example.com, a@example.com] produces:
- Processed: 3
- Unique: 2
- Duplicate occurrences: 1

## Run Locally

1. Install JDK 21.
2. Open the project in IntelliJ IDEA.
3. Reload Maven dependencies.
4. Run emailtool.EmailToolApplication.
5. Open http://localhost:8082/.

No database is required for the current implementation.

## REST Endpoint

POST /api/emails/deduplicate

Request: multipart/form-data with a file field named file.

Response: ZIP containing:
- unique-emails.xlsx
- duplicate-emails.xlsx

## Data Structures and Complexity

HashSet provides membership checking for normalized email addresses.
ArrayLists preserve the encounter order of the output entries.

Classification has expected O(n) time for bounded email lengths,
with O(u) set storage and O(n) output-list storage.

Excel parsing, workbook generation and ZIP buffers introduce additional
processing and memory costs.

## Sample Data

sample-data/email_test_5000.xlsx contains synthetic email addresses.

Expected results:
- Processed: 5,000
- Unique: 4,000
- Duplicate occurrences: 1,000

## Documentation

See [Technical Guide](docs/Email_Deduplication_Tool_Technical_Guide.docx)
for class explanations, flow diagrams, design decisions, complexity
analysis and testing guidance.

## Current Scope

This is a local portfolio application with synchronous processing.
It does not currently include authentication, persistent job history
or background processing.