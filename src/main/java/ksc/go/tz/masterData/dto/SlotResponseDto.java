package ksc.go.tz.masterData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.masterData.entities.Slot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Available slot details")
public class SlotResponseDto {

    @Schema(description = "Slot ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String slotId;
    @Schema(description = "Start of the available period", example = "2026-10-05T08:00:00")
    private LocalDateTime startDateTime;
    @Schema(description = "End of the available period", example = "2026-10-05T12:00:00")
    private LocalDateTime endDateTime;
    @Schema(description = "Record status", allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
    private String status;

    public SlotResponseDto(Slot slot) {
        this.slotId = slot.getId().toString();
        this.startDateTime = slot.getStartDateTime();
        this.endDateTime = slot.getEndDateTime();
        this.status = slot.getStatus().name();
    }
}
