package ksc.go.tz.contractAndSubscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Reason for rejecting or terminating a contract")
public class ContractReasonDto {

    @NotBlank(message = "Reason is required")
    @Size(max = 255)
    @Schema(description = "Why", example = "Client moved to another provider", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
