package ksc.go.tz.leads.dto;

import ksc.go.tz.enums.LeadSource;
import ksc.go.tz.enums.LeadStatus;
import ksc.go.tz.leads.entities.Leads;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeadsResponseDto {

    private String leadId;

    private String fullName;

    private String phoneNumber;

    private String email;

    private String serviceLine;

    private String message;

    private LeadSource source;

    private LeadStatus status;

    public LeadsResponseDto(Leads leads) {
        this.leadId = leads.getId().toString();
        this.fullName = leads.getFullName();
        this.phoneNumber = leads.getPhoneNumber();
        this.email = leads.getEmail();
        this.serviceLine = leads.getServiceLine();
        this.message = leads.getMessage();
        this.source = leads.getSource();
        this.status = leads.getStatus();

    }
}
