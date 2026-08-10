package com.relay.modules.file.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.file.domain.FileAttachment;
import com.relay.modules.file.repository.FileAttachmentRepository;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageProvider storageProvider;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @Transactional
    public FileAttachment uploadFile(MultipartFile file, Long uploaderId) {
        if (file.isEmpty()) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RelayException(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "File size exceeds limit (50MB)");
        }

        try {
            // Virus scanning integration point (interface only)
            // virusScanner.scan(file.getInputStream());

            String checksum = calculateChecksum(file);
            
            // Check for duplicate
            Optional<FileAttachment> existing = fileAttachmentRepository.findByChecksum(checksum);
            if (existing.isPresent()) {
                // Return a new reference to the same stored file
                FileAttachment attachment = createAttachmentRecord(file, existing.get().getStoredName(), existing.get().getThumbnailPath(), checksum, uploaderId);
                return fileAttachmentRepository.save(attachment);
            }

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.lastIndexOf(".") > 0) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            
            String storedName = UUID.randomUUID().toString() + extension;
            
            // Store file
            String storagePath = storageProvider.store(file, storedName);
            String thumbnailPath = null;
            
            // Generate thumbnail if image
            String mimeType = file.getContentType();
            if (mimeType != null && mimeType.startsWith("image/")) {
                thumbnailPath = generateAndStoreThumbnail(file, storedName);
            }

            FileAttachment attachment = createAttachmentRecord(file, storagePath, thumbnailPath, checksum, uploaderId);
            return fileAttachmentRepository.save(attachment);

        } catch (Exception e) {
            throw new RelayException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_FAILED", "Failed to upload file");
        }
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(String publicId) {
        FileAttachment attachment = fileAttachmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "File not found"));
                
        try {
            return storageProvider.load(attachment.getStoragePath());
        } catch (Exception e) {
            throw new RelayException(HttpStatus.INTERNAL_SERVER_ERROR, "LOAD_FAILED", "Failed to load file");
        }
    }

    @Transactional(readOnly = true)
    public Resource loadThumbnailAsResource(String publicId) {
        FileAttachment attachment = fileAttachmentRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "File not found"));
                
        if (attachment.getThumbnailPath() == null) {
            throw new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Thumbnail not available");
        }
                
        try {
            return storageProvider.load(attachment.getThumbnailPath());
        } catch (Exception e) {
            throw new RelayException(HttpStatus.INTERNAL_SERVER_ERROR, "LOAD_FAILED", "Failed to load thumbnail");
        }
    }

    private FileAttachment createAttachmentRecord(MultipartFile file, String storagePath, String thumbnailPath, String checksum, Long uploaderId) {
        User uploader = userRepository.getReferenceById(uploaderId);
        
        FileAttachment attachment = new FileAttachment();
        attachment.setUploadedBy(uploader);
        attachment.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        attachment.setStoredName(storagePath);
        attachment.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        
        String ext = "";
        if (attachment.getOriginalName().lastIndexOf(".") > 0) {
            ext = attachment.getOriginalName().substring(attachment.getOriginalName().lastIndexOf(".") + 1);
        }
        attachment.setExtension(ext.toLowerCase());
        
        attachment.setFileSize(file.getSize());
        attachment.setChecksum(checksum);
        attachment.setStorageProvider(storageProvider.getProviderName());
        attachment.setStoragePath(storagePath);
        attachment.setThumbnailPath(thumbnailPath);
        
        return attachment;
    }

    private String calculateChecksum(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String generateAndStoreThumbnail(MultipartFile file, String storedName) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) return null; // Not a valid image
            
            int targetWidth = 300;
            int targetHeight = (int) (originalImage.getHeight() * ((double) targetWidth / originalImage.getWidth()));
            
            Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "jpg", baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            
            String thumbnailName = "thumb_" + storedName + ".jpg";
            return storageProvider.storeThumbnail(is, thumbnailName);
        } catch (Exception e) {
            // Non-fatal, just no thumbnail
            return null;
        }
    }
}
