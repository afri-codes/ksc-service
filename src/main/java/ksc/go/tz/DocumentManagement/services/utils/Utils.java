package ksc.go.tz.DocumentManagement.services.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Utils {
    public static String generateUniqueFileName() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString();
        return date + uuid.replace("-", "");
    }
    public static String generateUniqueFileName(String ext) {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString();
        if(!ext.startsWith(".")){
            ext = "."+ext;
        }
        return date + uuid.replace("-", "")+ext;
    }



    public static String generateUniqueFileNames(String ext) {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString();
        if(!ext.startsWith(".")){
            ext = "."+ext;
        }
        return   uuid.replace("-", "")+ext;

    }
}
