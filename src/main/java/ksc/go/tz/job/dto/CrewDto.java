package ksc.go.tz.job.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CrewDto  {

    @NotNull(message = "Crew name must be provided")
    private String crewName;

    private String supervisorId;

    @NotNull(message = "Zone name must be provided")
    private String zoneName;

}
