package ksc.go.tz.contractAndSubscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ksc.go.tz.enums.Frequency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Create a contract from an ACCEPTED quote, or update a DRAFT contract. Service, site and default value come from the quote.")
public class ContractDto {

    @Schema(description = "ACCEPTED quote the contract is for (required on create; ignored on update)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String quoteId;

    @Schema(description = "Client the contract is with. Defaults to the site owner", example = "6b1f0c1e-5717-4562-b3fc-2c963f66afa6")
    private String clientId;

    @NotNull(message = "Start date is required")
    @Schema(description = "First day the contract is in force", example = "2026-10-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "Last day the contract is in force; must be on or after the start date", example = "2027-10-14", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Schema(description = "How often the service is delivered. Defaults to the quote's frequency", example = "MONTHLY")
    private Frequency frequency;

    @DecimalMin(value = "0.0", message = "Contract value must not be negative")
    @Schema(description = "Total contract value in TZS. Defaults to the quote total", example = "1680000.00")
    private BigDecimal contractValue;

    @Size(max = 255)
    @Schema(description = "Business name / description of the client", example = "Mikocheni Apartments Ltd")
    private String businessInfo;

    @Size(max = 50)
    @Schema(description = "Client's TIN (businesses)", example = "123-456-789")
    private String businessTin;

    @Size(max = 50)
    @Schema(description = "Client's BRELA registration number (businesses)", example = "BRELA-154872")
    private String businessBrelaNo;

    @Size(max = 50)
    @Schema(description = "Client's personal ID number, e.g. NIDA (individuals)", example = "19900101-12345-00001-12")
    private String personalIdNo;

    @Size(max = 4000)
    @Schema(description = "Contract terms printed on the PDF. Default terms are used when empty")
    private String terms;
}
