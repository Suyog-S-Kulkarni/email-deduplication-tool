package emailtool.api;

import emailtool.service.EmailProcessingService;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailProcessingService processingService;

    public EmailController(EmailProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping(
            value = "/deduplicate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> deduplicate(
            @RequestParam("file") MultipartFile file
    ) {
        var result = processingService.process(file);

        String disposition = ContentDisposition.attachment()
                .filename("email-deduplication-results.zip")
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(
                        "X-Total-Emails",
                        Integer.toString(result.totalEmails())
                )
                .header(
                        "X-Unique-Emails",
                        Integer.toString(result.uniqueEmails())
                )
                .header(
                        "X-Duplicate-Emails",
                        Integer.toString(result.duplicateEmails())
                )
                .cacheControl(CacheControl.noStore())
                .body(result.archive());
    }
}
