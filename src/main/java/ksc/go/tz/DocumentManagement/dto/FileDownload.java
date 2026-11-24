package ksc.go.tz.DocumentManagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FileDownload {

    @NotBlank
    private UUID employeeId;

    @NotBlank
    private String fileId;
}
