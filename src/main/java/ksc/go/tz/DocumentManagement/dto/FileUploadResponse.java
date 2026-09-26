package ksc.go.tz.DocumentManagement.dto;

import ksc.go.tz.DocumentManagement.entities.Upload;
import ksc.go.tz.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FileUploadResponse {

    private String fileId;
    private String description;
    private DocumentType documentType;
    private String fileName;

    public FileUploadResponse(Upload upload) {
        this.fileId = upload.getId().toString();
        this.description = upload.getFileDescription();
        this.documentType = upload.getFileType();
        this.fileName = upload.getFileName();
    }

}
