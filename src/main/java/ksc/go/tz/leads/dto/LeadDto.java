package ksc.go.tz.leads.dto;


import jakarta.validation.constraints.NotNull;
import ksc.go.tz.enums.LeadSource;
import ksc.go.tz.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeadDto {

   @NotNull(message = "Full name must be provided")
    private String fullName;

   @NotNull(message = "Phone number must be provided")
    private String phoneNumber;

   @NotNull(message = "Email must be provided")
    private String email;

   @NotNull(message = "Service line must be provided")
    private String serviceLine;

    private String message;

    @NotNull(message = "Lead source must be provided")
    private LeadSource source;

    private LeadStatus status;
}
