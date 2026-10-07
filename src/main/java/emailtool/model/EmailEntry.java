package emailtool.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailEntry(

        int sourceRow,

        @NotBlank
        @Email
        @Size(max = 254)
        String email

) {
}
