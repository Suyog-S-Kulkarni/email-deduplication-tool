package emailtool.service;

import emailtool.model.DeduplicationResult;
import emailtool.model.EmailEntry;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EmailProcessingService {

    private final ExcelEmailReader reader;
    private final EmailDeduplicator deduplicator;
    private final ExcelArchiveWriter archiveWriter;

    public EmailProcessingService(
            ExcelEmailReader reader,
            EmailDeduplicator deduplicator,
            ExcelArchiveWriter archiveWriter
    ) {
        this.reader = reader;
        this.deduplicator = deduplicator;
        this.archiveWriter = archiveWriter;
    }

    public ProcessingDownload process(MultipartFile file) {
        List<EmailEntry> emails = reader.read(file);

        DeduplicationResult result =
                deduplicator.deduplicate(emails);

        byte[] archive = archiveWriter.writeArchive(result);

        return new ProcessingDownload(
                archive,
                result.totalEmails(),
                result.uniqueEmails().size(),
                result.duplicateEmails().size()
        );
    }

    public record ProcessingDownload(
            byte[] archive,
            int totalEmails,
            int uniqueEmails,
            int duplicateEmails
    ) {
    }
}
