package ksc.go.tz.DocumentManagement.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.DocumentManagement.dto.FileDownload;
import ksc.go.tz.DocumentManagement.dto.FileMetaData;
import ksc.go.tz.DocumentManagement.entities.Upload;
import ksc.go.tz.DocumentManagement.repositories.UploadRepository;
import ksc.go.tz.DocumentManagement.services.utils.Utils;
import ksc.go.tz.common.configs.DocumentTypeFolderConfig;
import ksc.go.tz.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final UploadRepository uploadRepository;
    private final DocumentTypeFolderConfig documentTypeFolderConfig;
    @Value("${file.upload-dir}")
    private String uploadDir;
    private static final Map<String, String> mimeTypesMap = new HashMap<>();
    private static final Map<String, String> fileExtenstionsMap = new HashMap<>();

    static {
        mimeTypesMap.put("pdf", "application/pdf");
        mimeTypesMap.put("png", "image/png");
        mimeTypesMap.put("jpg", "image/jpeg");
        mimeTypesMap.put("jpeg", "image/jpeg");
        mimeTypesMap.put("txt", "text/plain");
        fileExtenstionsMap.put("application/pdf", "pdf");
        fileExtenstionsMap.put("image/png", "png");
        fileExtenstionsMap.put("image/jpeg", "jpeg");
        fileExtenstionsMap.put("image/jpg", "jpeg");
        fileExtenstionsMap.put("text/plain", "txt");
    }

    @Override
    @Transactional
    public Upload uploadFileBase64(String base64Data, String fileDescription, DocumentType documentType) {


            Upload existingUpload = null;
            log.info("Existing upload for employee {}: {}", existingUpload != null ? existingUpload.getFileName() : "None");
            if (existingUpload != null) {
                log.info("Deleting existing upload for employee {}: {}", existingUpload.getFileName());
                String existingFilePath = getFolder(existingUpload.getFileType()) + "/" + existingUpload.getFileName();
                File existingFile = new File(existingFilePath);
                if (existingFile.exists()) {
                    if (existingFile.delete()) {
                        log.info("Deleted existing file: {}", existingFilePath);
                    } else {
                        log.warn("Failed to delete existing file: {}", existingFilePath);
                    }
                }
                uploadRepository.delete(existingUpload);
            }

            byte[] data = Base64.getDecoder().decode(base64Data);
            String savedFileName = saveFileToDisks(data, documentType);
            Upload upload = new Upload();
            upload.setFileName(savedFileName);
            upload.setFileDescription(fileDescription);
            upload.setFileType(documentType);
//            upload.setCheckNumber(employee.get().getCheckNumber());
            upload.setCreatedBy(null);
            upload.setCreatedAt(LocalDateTime.now());
            var savedUpload=  uploadRepository.save(upload);

            return savedUpload;

    }

    private String saveFileToDisks(byte[] data, DocumentType documentType) {
        Tika tika = new Tika();
        String extension = fileExtenstionsMap.getOrDefault(tika.detect(data), "pdf");

        String savedFileName = Utils.generateUniqueFileNames(extension);

        String directoryPath = getFolder(documentType) ;
        log.info("Directory Path: {}", directoryPath);
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // create all parent directories
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + directoryPath);
            }
        }

        String filePath = directoryPath + "/" + savedFileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            log.info("Saving file to path: {}", filePath);
            fos.write(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("File saved successfully: {}", savedFileName);
        return savedFileName;
    }

    private String saveFileToDisk(byte[] data, DocumentType documentType) {
        Tika tika = new Tika();
        String extension = fileExtenstionsMap.getOrDefault(tika.detect(data), "pdf");

//        String savedFileName = UUID.randomUUID().toString().concat(".").concat(extension);
        String savedFileName = Utils.generateUniqueFileName(extension);
        String filePath = getFolder(documentType) + "/" + savedFileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return savedFileName;
    }

    private String saveFileToDisk(MultipartFile file, DocumentType documentType) {
        String savedFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        String filePath = uploadDir + "/" + savedFileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return savedFileName;
    }

    @Override
    public FileMetaData downloadFile(FileDownload fileDownload) {
        Upload upload = getFileById(fileDownload.getFileId());
        if (upload == null) throw new AfriException("Could find specified file for download");
        return doDownLoad(upload, fileDownload);
    }
    @Override
    public FileMetaData downloadFileByName(String fileName) {
        Upload upload = getFileByName(fileName);
        if (upload == null) throw new AfriException("Could find specified file for download");
        return doDownLoad(upload,null);
    }


    private Upload getFileByName(String fileName) {
        return uploadRepository.findByFileName(fileName).orElse(null);
    }

    @Override
    public FileMetaData downloadBase64File(String id) {
        Upload upload = getFileById(id);
        if (upload == null) throw new AfriException("Could not find specified file for download");
        return doDownLoad(upload,null);
    }

    @Override
    public FileMetaData downloadBase64FileByName(String fileName) {
        Upload upload = getFileByName(fileName);
        if (upload == null) throw new AfriException("Could not find specified file for download");
        return doDownLoad(upload, null);
    }

    @Override
    public Upload getFileById(String id) {
        return uploadRepository.findById(UUID.fromString(id)).orElse(null);
    }

    public String getContentType(String filePath) {

        // Use java.nio.file.Files to determine content type from the file
        try {
            Path path = Paths.get(filePath);
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                String extension = getFileExtension(filePath);
                return mimeTypesMap.getOrDefault(extension, "application/octet-stream");
            }
            return contentType;
        } catch (IOException e) {
            throw new RuntimeException("Failed to determine content type", e);
        }
    }

    private String getFileExtension(String filePath) {
        String extension = "";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1).toLowerCase();
        }
        return extension;
    }

    private static String getMimeType(byte[] data) throws IOException {
        // Use ByteArrayInputStream to create an input stream from byte array
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        // Guess MIME type from the stream
        String mimeType = URLConnection.guessContentTypeFromStream(stream);
        return mimeType;
    }

    public String convertBytesToBase64(byte[] fileData) {
        // Use the Base64 encoder to encode the byte array
        String base64File = Base64.getEncoder().encodeToString(fileData);
        return base64File;
    }

    @Override
    public Upload uploadMultipartFile(MultipartFile file, String fileDescription, DocumentType documentType,UUID createdBy) {
        String fileName = saveMultipartFileToDisk(file, documentType);
        Upload fileEntity = new Upload();
        fileEntity.setId(UUID.randomUUID());
        fileEntity.setFileDescription(fileDescription == null ? file.getOriginalFilename() : fileDescription);
        fileEntity.setFileName(fileName);
        fileEntity.setFileType(documentType);
        fileEntity.setCreatedBy(createdBy);
        return uploadRepository.save(fileEntity);
    }

    private String saveMultipartFileToDisk(MultipartFile file, DocumentType documentType) {

        String savedFileName = Utils.generateUniqueFileName(Objects.requireNonNull(FilenameUtils.getExtension(file.getOriginalFilename())));
        String folder = getFolder(documentType);
        checkOrCreateFolder(folder);
        String filePath = folder + "/" + savedFileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new AfriException(e.getMessage());
        }
        return savedFileName;
    }

    private String getFolder(DocumentType documentType) {
        log.info("Getting folder for document type: {}", documentType.name());
        var p = documentTypeFolderConfig.getPaths();
        if(p == null){
            throw new AfriException("Destination folder for " + documentType.name() + " is not properly configured");
        }else{
            String path = p.getOrDefault(documentType.name(),p.get("DEFAULT"));
            log.info("Resolved folder path: {}", path);
            return Objects.requireNonNull( path, "Destination folder for " + documentType.name() + " is not properly configured");

        }



    }

    private void checkOrCreateFolder(String folderPath) {
        File folder = new File(folderPath);
        if ((!folder.exists())) throw new AfriException("Upload folder not configured");
        /*
        if (!folder.exists()) {
            boolean isCreated = folder.mkdirs();
            if (isCreated) {
                log.info("Folder created: {}", folderPath);
            } else {
                throw new JscException("Failed to create folder for this file upload ");
            }
        }

         */
    }

    private FileMetaData doDownLoad(Upload upload, FileDownload fileDownload) {
        if (upload == null) throw new AfriException("Could not find specified file for download");
        try {
//            Optional<Employee> employee = employeeRepository.findById(fileDownload.getEmployeeId());
//            if (employee.isEmpty()) {
//                throw new AfriException("Employee with ID " + fileDownload.getEmployeeId() + " not found");
//            }

            String filePath = getFolder(upload.getFileType())+ "/" + upload.getFileName();
            log.info("Going to find to {}",filePath);
            var contentType = getContentType(filePath);
            var bts = Files.readAllBytes(Paths.get(filePath));
            var downloadable = new FileMetaData(convertBytesToBase64(bts), contentType, upload.getFileName());
            downloadable.setFileDescription(upload.getFileDescription());
            downloadable.setBytes(bts);
            return downloadable;
        } catch (NoSuchFileException e){
            throw new AfriException("No Such File");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
