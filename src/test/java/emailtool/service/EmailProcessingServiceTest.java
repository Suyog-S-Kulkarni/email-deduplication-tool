package emailtool.service;

import emailtool.exception.InvalidExcelException;

import jakarta.validation.Validation;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.HashMap;
import java.util.Map;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailProcessingServiceTest {

    @Test
    void normalizesEmailsAndProducesTwoReadableWorkbooks() throws Exception {
        MockMultipartFile input = excelFile(
                " Suyog@Example.com ",
                "admin@example.com",
                "suyog@example.com"
        );

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            EmailProcessingService service = new EmailProcessingService(
                    new ExcelEmailReader(factory.getValidator()),
                    new EmailDeduplicator(),
                    new ExcelArchiveWriter()
            );

            var result = service.process(input);

            assertThat(result.totalEmails()).isEqualTo(3);
            assertThat(result.uniqueEmails()).isEqualTo(2);
            assertThat(result.duplicateEmails()).isEqualTo(1);

            Map<String, byte[]> files = unzip(result.archive());

            assertThat(files).containsOnlyKeys(
                    "unique-emails.xlsx",
                    "duplicate-emails.xlsx"
            );

            try (var uniqueWorkbook = new XSSFWorkbook(
                    new ByteArrayInputStream(
                            files.get("unique-emails.xlsx")
                    ))) {

                var sheet = uniqueWorkbook.getSheetAt(0);

                // Header plus two unique rows.
                assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);

                assertThat(sheet.getRow(1).getCell(1).getStringCellValue())
                        .isEqualTo("suyog@example.com");

                assertThat(sheet.getRow(2).getCell(1).getStringCellValue())
                        .isEqualTo("admin@example.com");
            }

            try (var duplicateWorkbook = new XSSFWorkbook(
                    new ByteArrayInputStream(
                            files.get("duplicate-emails.xlsx")
                    ))) {

                var sheet = duplicateWorkbook.getSheetAt(0);

                assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);

                assertThat(sheet.getRow(1).getCell(0).getNumericCellValue())
                        .isEqualTo(4.0);

                assertThat(sheet.getRow(1).getCell(1).getStringCellValue())
                        .isEqualTo("suyog@example.com");
            }
        }
    }

    @Test
    void rejectsInvalidEmailWithSourceRowNumber() throws Exception {
        MockMultipartFile input = excelFile("not-an-email");

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var reader = new ExcelEmailReader(factory.getValidator());

            assertThatThrownBy(() -> reader.read(input))
                    .isInstanceOf(InvalidExcelException.class)
                    .hasMessageContaining("row 2");
        }
    }

    private MockMultipartFile excelFile(String... emails) throws Exception {
        try (var workbook = new XSSFWorkbook();
             var output = new ByteArrayOutputStream()) {

            var sheet = workbook.createSheet("Emails");
            sheet.createRow(0).createCell(0).setCellValue("Email");

            for (int i = 0; i < emails.length; i++) {
                sheet.createRow(i + 1)
                        .createCell(0)
                        .setCellValue(emails[i]);
            }

            workbook.write(output);

            return new MockMultipartFile(
                    "file",
                    "emails.xlsx",
                    "application/vnd.openxmlformats-officedocument"
                            + ".spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }

    private Map<String, byte[]> unzip(byte[] archive) throws Exception {
        Map<String, byte[]> files = new HashMap<>();

        try (var zip = new ZipInputStream(
                new ByteArrayInputStream(archive))) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                files.put(entry.getName(), zip.readAllBytes());
                zip.closeEntry();
            }
        }

        return files;
    }
}
