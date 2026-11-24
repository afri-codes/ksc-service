package ksc.go.tz.DocumentManagement.services;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ksc.go.tz.DocumentManagement.dto.FileDownload;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.DocumentManagement.dto.FileUploadResponse;
import ksc.go.tz.DocumentManagement.entities.Upload;
import ksc.go.tz.enums.DocumentType;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {
    Upload uploadMultipartFile(MultipartFile file, String fileDescription, DocumentType documentType, UUID userId);
    FileUploadResponse uploadFileBase64(@NotBlank String base64File, String description, @NotNull DocumentType documentType, Authentication authentication);

    FileMetaData downloadFile(FileDownload fileDownload);
    FileMetaData downloadBase64File(String id);

    Upload getFileById(String id);

    FileMetaData downloadBase64FileByName(String fileName);

    FileMetaData downloadFileByName(String fileName);

}
