package emailtool.service;


import emailtool.model.DeduplicationResult;
import emailtool.model.EmailEntry;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ExcelArchiveWriter {

    public byte[] writeArchive(DeduplicationResult result) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            try (ZipOutputStream zip = new ZipOutputStream(output)) {
                addWorkbook(
                        zip,
                        "unique-emails.xlsx",
                        "Unique Emails",
                        result.uniqueEmails()
                );

                addWorkbook(
                        zip,
                        "duplicate-emails.xlsx",
                        "Duplicate Emails",
                        result.duplicateEmails()
                );
            }

            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to generate the result archive.",
                    exception
            );
        }
    }

    private void addWorkbook(
            ZipOutputStream zip,
            String filename,
            String sheetName,
            List<EmailEntry> entries
    ) throws IOException {

        // Build each workbook separately so its lifecycle does not
        // close the outer ZIP stream.
        byte[] workbookBytes = createWorkbook(sheetName, entries);

        zip.putNextEntry(new ZipEntry(filename));
        zip.write(workbookBytes);
        zip.closeEntry();
    }

    private byte[] createWorkbook(
            String sheetName,
            List<EmailEntry> entries
    ) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Source Row");
            header.createCell(1).setCellValue("Email");

            header.getCell(0).setCellStyle(headerStyle);
            header.getCell(1).setCellStyle(headerStyle);

            int outputRow = 1;

            for (EmailEntry entry : entries) {
                Row row = sheet.createRow(outputRow++);

                row.createCell(0).setCellValue(entry.sourceRow());

                // Writes a string cell, not an Excel formula.
                row.createCell(1).setCellValue(entry.email());
            }

            sheet.createFreezePane(0, 1);
            sheet.setColumnWidth(0, 14 * 256);
            sheet.setColumnWidth(1, 60 * 256);

            workbook.write(output);

            return output.toByteArray();
        }
    }
}
