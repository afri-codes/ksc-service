package ksc.go.tz.contractAndSubscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Record that the client signed the contract")
public class ContractSignDto {

    @NotBlank(message = "Name of the person who signed is required")
    @Size(max = 255)
    @Schema(description = "Name of the person who signed for the client", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String signedBy;

    @PastOrPresent(message = "Signed time cannot be in the future")
    @Schema(description = "When it was signed. Defaults to now", example = "2026-10-10T14:30:00")
    private LocalDateTime signedAt;
}
