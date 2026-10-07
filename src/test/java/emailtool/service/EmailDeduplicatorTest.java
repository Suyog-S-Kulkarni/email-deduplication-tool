package emailtool.service;

import emailtool.model.EmailEntry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDeduplicatorTest {

    private final EmailDeduplicator deduplicator =
            new EmailDeduplicator();

    @Test
    void keepsFirstOccurrenceAndSeparatesEveryRepeat() {
        List<EmailEntry> input = List.of(
                new EmailEntry(2, "suyog@example.com"),
                new EmailEntry(3, "admin@example.com"),
                new EmailEntry(4, "suyog@example.com"),
                new EmailEntry(5, "suyog@example.com")
        );

        var result = deduplicator.deduplicate(input);

        assertThat(result.uniqueEmails()).containsExactly(
                new EmailEntry(2, "suyog@example.com"),
                new EmailEntry(3, "admin@example.com")
        );

        assertThat(result.duplicateEmails()).containsExactly(
                new EmailEntry(4, "suyog@example.com"),
                new EmailEntry(5, "suyog@example.com")
        );

        assertThat(result.totalEmails()).isEqualTo(4);
    }

    @Test
    void returnsNoDuplicatesWhenEveryAddressIsDifferent() {
        var result = deduplicator.deduplicate(List.of(
                new EmailEntry(2, "first@example.com"),
                new EmailEntry(3, "second@example.com")
        ));

        assertThat(result.uniqueEmails()).hasSize(2);
        assertThat(result.duplicateEmails()).isEmpty();
    }
}
