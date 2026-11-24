package ksc.go.tz.masterData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for creating or updating an available slot (date range)")
public class SlotDto {

    @NotNull(message = "Start date time is required")
    @Schema(description = "Start of the available period (ISO-8601)", example = "2026-10-05T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    @Schema(description = "End of the available period (ISO-8601). Must be after startDateTime", example = "2026-10-05T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endDateTime;

}
