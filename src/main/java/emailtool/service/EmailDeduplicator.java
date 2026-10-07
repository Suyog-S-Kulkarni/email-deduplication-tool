package emailtool.service;

import emailtool.model.DeduplicationResult;
import emailtool.model.EmailEntry;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EmailDeduplicator {

    public DeduplicationResult deduplicate(List<EmailEntry> emails) {
        Set<String> seenEmails = new HashSet<>();

        List<EmailEntry> uniqueEmails = new ArrayList<>();
        List<EmailEntry> duplicateEmails = new ArrayList<>();

        for (EmailEntry entry : emails) {
            boolean firstOccurrence = seenEmails.add(entry.email());

            if (firstOccurrence) {
                uniqueEmails.add(entry);
            } else {
                duplicateEmails.add(entry);
            }
        }

        return new DeduplicationResult(
                uniqueEmails,
                duplicateEmails
        );
    }
}
