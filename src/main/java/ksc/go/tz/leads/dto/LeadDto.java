package ksc.go.tz.leads.dto;


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

    private String fullName;

    private String phoneNumber;

    private String email;

    private String serviceLine;

    private String message;

    private LeadSource source;

    private LeadStatus status;
}
