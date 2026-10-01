package ksc.go.tz.masterData.dto;

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
public class SlotResponseDto {

    private String slotId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;

    public SlotResponseDto(Slot slot) {
        this.slotId = slot.getId().toString();
        this.startDateTime = slot.getStartDateTime();
        this.endDateTime = slot.getEndDateTime();
        this.status = slot.getStatus().name();
    }
}
