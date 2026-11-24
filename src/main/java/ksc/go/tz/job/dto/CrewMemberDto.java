package ksc.go.tz.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Add a staff member to a crew")
public class CrewMemberDto {

    @NotBlank(message = "Staff ID is required")
    @Schema(description = "The staff member's user ID", example = "6b1f0c1e-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String staffId;

    @Size(max = 255)
    @Schema(description = "Display name", example = "Amina Juma")
    private String staffName;

    @Size(max = 100)
    @Schema(description = "Role in the crew", example = "Cleaner")
    private String memberRole;

    @Schema(description = "Date they joined the crew. Defaults to today", example = "2026-10-03")
    private LocalDate joinedOn;
}
