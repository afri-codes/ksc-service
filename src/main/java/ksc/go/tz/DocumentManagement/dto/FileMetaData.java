package ksc.go.tz.DocumentManagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileMetaData {
    private byte[] bytes;
    private String contentType;
    private String fileName;
    private String base64;
    private String fileDescription = "";

    public FileMetaData(byte[] bytes, String contentType, String fileName) {
        this.bytes = bytes;
        this.contentType = contentType;
        this.fileName = fileName;
    }
    public FileMetaData(String base64, String contentType, String fileName) {
        this.base64 = base64;
        this.contentType = contentType;
        this.fileName = fileName;
    }
}
