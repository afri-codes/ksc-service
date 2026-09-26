package ksc.go.tz.DocumentManagement.controllers;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.enums.DocumentType;
import afriUtils.enums.ResponseEnum;
import afriUtils.responses.AfriException;
import afriUtils.responses.ApiResponseUtil;
import jakarta.validation.Valid;
import ksc.go.tz.DocumentManagement.dto.FileDownload;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.DocumentManagement.dto.FileUpload;
import ksc.go.tz.DocumentManagement.entities.Upload;
import ksc.go.tz.DocumentManagement.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayInputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file-upload")

public class FileController {
    private final FileService fileService;
    private final ApiResponseUtil apiResponseUtil;
    private final AuthDetailsExtractor authDetailsExtractor;

    @PostMapping("/uploads")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("documentType") DocumentType documentType,
                                        @RequestParam("description") String description,Authentication authentication
    ) {
        Upload uploadedFile = fileService.uploadMultipartFile(file, description, documentType,authDetailsExtractor.getUserId(authentication));
        return apiResponseUtil.getResponse(uploadedFile, ResponseEnum.SUCCESS);
    }

    @PostMapping("/uploads/base64/multipart")
    public ResponseEntity<?> uploadFileBase64Multipart(@RequestParam("base64File") String base64Data,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("documentType") DocumentType documentType,Authentication authentication) {
        try {
            Upload uploadedFile = fileService.uploadFileBase64(base64Data, description, documentType);
            return apiResponseUtil.getResponse(uploadedFile, ResponseEnum.SUCCESS);
        } catch (Exception e) {
            throw new AfriException(e.getMessage());
        }
    }

    @PostMapping("/uploads/base64")
    public ResponseEntity<?> uploadFileBase64(@RequestBody  @Valid FileUpload upload) {
        try {
            Upload uploadedFile = fileService.uploadFileBase64(upload.getBase64File(), "portalDocs", DocumentType.CONTRACT);
            return apiResponseUtil.getResponse(uploadedFile, ResponseEnum.SUCCESS);
        } catch (Exception e) {
            throw new AfriException(e.getMessage());
        }
    }

    @GetMapping("/download/file")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestBody  @Valid FileDownload upload) {
        try {
            FileMetaData fileMetaData = fileService.downloadFile(upload);

            if (fileMetaData.getBytes() != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileMetaData.getFileName());
                headers.setContentType(MediaType.parseMediaType(fileMetaData.getContentType()));  // Set correct Content-Type

                return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(fileMetaData.getBytes())), headers, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new AfriException(e.getMessage());
        }
    }

    @GetMapping("/download/base64/{id}")
    public ResponseEntity<?> downloadBase64File(@PathVariable String id) {
        FileMetaData fileMetaData = fileService.downloadBase64File(id);
        return apiResponseUtil.getResponse(fileMetaData);

    }


    @GetMapping("/download/base64/names/{fileName}")
    public ResponseEntity<?> downloadBase64FileByName(@PathVariable String fileName) {
        FileMetaData fileMetaData = fileService.downloadBase64FileByName(fileName);
        return apiResponseUtil.getResponse(fileMetaData);

    }

    @GetMapping("/download/names/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFileByName(@PathVariable String fileName) {
        try {
            FileMetaData fileMetaData = fileService.downloadFileByName(fileName);
            if (fileMetaData.getBytes() != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileMetaData.getFileName());
                headers.setContentType(MediaType.parseMediaType(fileMetaData.getContentType()));  // Set correct Content-Type

                return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(fileMetaData.getBytes())), headers, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new AfriException(e.getMessage());
        }
    }



}
