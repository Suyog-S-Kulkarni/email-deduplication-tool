package emailtool.model;

import java.util.List;

public record DeduplicationResult(
        List<EmailEntry> uniqueEmails,
        List<EmailEntry> duplicateEmails
) {

    public DeduplicationResult {
        uniqueEmails = List.copyOf(uniqueEmails);
        duplicateEmails = List.copyOf(duplicateEmails);
    }

    public int totalEmails() {
        return uniqueEmails.size() + duplicateEmails.size();
    }
}