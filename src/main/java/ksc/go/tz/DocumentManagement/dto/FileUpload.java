package ksc.go.tz.DocumentManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ksc.go.tz.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FileUpload {
    @NotBlank(message = "base64File Must be provided")
    private String base64File;
    private String description;
    @NotNull(message = "Document type must be provided")
    private DocumentType documentType;

}
