package com.pcoundia.helper.app;

import com.pcoundia.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FileHelper {

    private static final Map<String, String> mimeToExtension = new HashMap<>();

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    @Value("${file.save-path::tmp}")
    private String baseSavePath;

    static {
        // Image MIME types and extensions
        mimeToExtension.put("image/png", ".png");
        mimeToExtension.put("image/jpeg", ".jpg");
        mimeToExtension.put("image/gif", ".gif");
        mimeToExtension.put("image/bmp", ".bmp");
        mimeToExtension.put("image/tiff", ".tiff");

        // Document MIME types and extensions
        mimeToExtension.put("application/pdf", ".pdf");
        mimeToExtension.put("application/msword", ".doc");
        mimeToExtension.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        mimeToExtension.put("application/vnd.ms-excel", ".xls");
        mimeToExtension.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        mimeToExtension.put("application/vnd.ms-powerpoint", ".ppt");
        mimeToExtension.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
        mimeToExtension.put("text/plain", ".txt");
        // Add more MIME types and extensions as needed
    }

    public String saveDataUriFile(String dataUri, String filePath) throws IOException {
        String base64Data = extractBase64Data(dataUri);
        // Decode the Base64 data
        // byte[] binaryData = Base64.decodeBase64(base64Data);
        byte[] binaryData = Base64Utils.decodeFromString(base64Data);

        // Check if the file size is within the allowed limit
        if (binaryData.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit.");
        }

        // Extract the file extension from the Data URI Base64 data
        String fileExtension = extractFileExtension(dataUri);

        // Combine the file path and extension
        String completeFilePath = baseSavePath + filePath + fileExtension;

        // System.out.println(completeFilePath);

        // Create directories if they don't exist
        Path directoryPath = Path.of(completeFilePath).getParent();
        if (directoryPath != null && !Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Save the decoded data to a file
        try (FileOutputStream fos = new FileOutputStream(completeFilePath)) {
            fos.write(binaryData);
            log.info("File saved in {}", filePath+fileExtension);
        } catch (Exception e) {
            log.error("Error saving file: {}", e.getMessage(), e);
        }


        return filePath+fileExtension;
    }

    public String saveDataUriFile(String dataUri, String filePath, String oldFilePath) throws IOException {
        String newFilePath = saveDataUriFile(dataUri, filePath);
        // We delete the oldFile

        // Combine the file path and extension
        String completeFilePath = baseSavePath + oldFilePath;
        Path fileToDeletePath = Path.of(completeFilePath);
        // File oldFile = new File(completeFilePath);
        try {
            boolean isFileDeleted = Files.deleteIfExists(fileToDeletePath);
            if (isFileDeleted)
                log.info("Old File of path {} deleted", oldFilePath);
            else
                log.info("Old File of path {} doesn't exist. So nothing was deleted", oldFilePath);
        } catch (DirectoryNotEmptyException ex) {
            log.error("Directory not empty ", ex);
        } catch (SecurityException ex) {
            log.error("Access denied. Could not delete file", ex);
        } catch (Exception ex) {
            log.warn("File could not be deleted", ex);
        }
        return newFilePath;
    }

    public String saveFile(MultipartFile file, String filePath) {
        try {
            // Retrieve the file extension
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null)
                throw new ApiException("Original Filename not found");
            String fileExtension = getFileExtension(originalFileName);

            // Combine the file path and extension
            String completeFilePath = baseSavePath + filePath + fileExtension;

            // Create directories if they don't exist
            Path directoryPath = Path.of(completeFilePath).getParent();
            if (directoryPath != null && !Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Construct the full path to save the file
            Path path = Path.of(completeFilePath);

            // Copy the file to the specified location
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Return the file path
            return filePath+fileExtension;
        } catch (Exception e) {
            // Handle the exception (e.g., log it or throw a custom exception)
            throw new ApiException("Failed to save the file: " + e.getMessage());
        }

    }

    public String saveFile(MultipartFile file, String filePath, String oldFilePath) {
        try {
            if (oldFilePath != null) {
                // We delete the oldFile
                // Combine the file path and extension
                String completeFilePath = baseSavePath + oldFilePath;
                Path fileToDeletePath = Path.of(completeFilePath);
                // File oldFile = new File(completeFilePath);
                try {
                    boolean isFileDeleted = Files.deleteIfExists(fileToDeletePath);
                    if (isFileDeleted)
                        log.info("Old File of path {} deleted", oldFilePath);
                    else
                        log.info("Old File of path {} doesn't exist. So nothing was deleted", oldFilePath);
                } catch (DirectoryNotEmptyException ex) {
                    log.error("Directory not empty ", ex);
                } catch (SecurityException ex) {
                    log.error("Access denied. Could not delete file", ex);
                } catch (Exception ex) {
                    log.warn("File could not be deleted", ex);
                }
            }

            return saveFile(file, filePath);
        } catch (Exception e) {
            // Handle the exception (e.g., log it or throw a custom exception)
            throw new ApiException("Failed to save the file: " + e.getMessage());
        }

    }


    private String extractBase64Data(String dataUri) {
        Pattern pattern = Pattern.compile("data:[^;]*;base64,(.*)");
        Matcher matcher = pattern.matcher(dataUri);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid Data URI format");
    }

    private String extractFileExtension(String dataUri) {
        Pattern pattern = Pattern.compile("^data:([a-zA-Z0-9/.-]+);base64,");
        Matcher matcher = pattern.matcher(dataUri);

        if (matcher.find()) {
            String mimeType = matcher.group(1);
            String extension = mimeToExtension.getOrDefault(mimeType, ".bin");
            return extension;
        }

        // Default to a specific extension if MIME type detection fails
        return ".bin";
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            // Handle the case where there is no file extension (optional)
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase();
    }

}
