package ksc.go.tz.DocumentManagement.services;



import ksc.go.tz.DocumentManagement.dto.FileDownload;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.DocumentManagement.entities.Upload;
import ksc.go.tz.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {
    Upload uploadMultipartFile(MultipartFile file, String fileDescription, DocumentType documentType, UUID userId);
    Upload uploadFileBase64(String base64Data, String fileDescription,DocumentType documentType);

    FileMetaData downloadFile(FileDownload fileDownload);
    FileMetaData downloadBase64File(String id);

    Upload getFileById(String id);

    FileMetaData downloadBase64FileByName(String fileName);

    FileMetaData downloadFileByName(String fileName);

}
