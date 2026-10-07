package emailtool.service;

import emailtool.exception.InvalidExcelException;
import emailtool.model.EmailEntry;

import jakarta.validation.Validator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ExcelEmailReader {

    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    private static final int MAX_DATA_ROWS = 10_000;
    private static final int MAX_COLUMNS = 50;

    private final Validator validator;

    public ExcelEmailReader(Validator validator) {
        this.validator = validator;
    }

    public List<EmailEntry> read(MultipartFile file) {
        validateUpload(file);

        try (InputStream input = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(input)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidExcelException(
                        "The workbook must contain a worksheet."
                );
            }

            // Version 1 processes only the first worksheet.
            Sheet sheet = workbook.getSheetAt(0);

            // Header is row index 0. Data occupies indices 1 through 10000.
            if (sheet.getLastRowNum() > MAX_DATA_ROWS) {
                throw new InvalidExcelException(
                        "The first worksheet must not extend beyond "
                                + "10,000 data rows."
                );
            }

            int emailColumn = findEmailColumn(sheet.getRow(0));

            List<EmailEntry> emails = new ArrayList<>();

            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                if (row.getLastCellNum() > MAX_COLUMNS) {
                    throw new InvalidExcelException(
                            "Row " + (rowIndex + 1)
                                    + " exceeds the 50-column limit."
                    );
                }

                Cell cell = row.getCell(
                        emailColumn,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                );

                if (cell == null) {
                    continue;
                }

                if (cell.getCellType() != CellType.STRING) {
                    throw new InvalidExcelException(
                            "The Email cell at row " + (rowIndex + 1)
                                    + " must contain plain text. "
                                    + "Numbers, formulas, and error cells "
                                    + "are not supported."
                    );
                }

                String rawEmail = cell.getStringCellValue().strip();

                if (rawEmail.isEmpty()) {
                    continue;
                }

                String normalizedEmail =
                        rawEmail.toLowerCase(Locale.ROOT);

                EmailEntry entry = new EmailEntry(
                        rowIndex + 1,
                        normalizedEmail
                );

                if (!validator.validate(entry).isEmpty()) {
                    throw new InvalidExcelException(
                            "Invalid email at Excel row "
                                    + entry.sourceRow()
                                    + ". Correct the value and upload again."
                    );
                }

                emails.add(entry);
            }

            if (emails.isEmpty()) {
                throw new InvalidExcelException(
                        "No nonblank email addresses were found."
                );
            }

            return List.copyOf(emails);

        } catch (InvalidExcelException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new InvalidExcelException(
                    "Cannot read this workbook. Upload a valid, "
                            + "unencrypted .xlsx file.",
                    exception
            );
        }
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidExcelException(
                    "Select a nonempty Excel file."
            );
        }

        if (file.getSize() > MAX_FILE_BYTES) {
            throw new InvalidExcelException(
                    "The file must be no larger than 5 MB."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new InvalidExcelException(
                    "Only .xlsx files are supported."
            );
        }
    }

    private int findEmailColumn(Row header) {
        if (header == null) {
            throw new InvalidExcelException(
                    "The first row must contain an Email header."
            );
        }

        if (header.getLastCellNum() > MAX_COLUMNS) {
            throw new InvalidExcelException(
                    "The worksheet must contain no more than 50 columns."
            );
        }

        int emailColumn = -1;

        for (Cell cell : header) {
            if (cell.getCellType() != CellType.STRING) {
                continue;
            }

            String value = cell.getStringCellValue().strip();

            if ("email".equalsIgnoreCase(value)) {
                if (emailColumn != -1) {
                    throw new InvalidExcelException(
                            "The worksheet contains multiple Email headers."
                    );
                }

                emailColumn = cell.getColumnIndex();
            }
        }

        if (emailColumn == -1) {
            throw new InvalidExcelException(
                    "The first row must contain a column named Email."
            );
        }

        return emailColumn;
    }
}
