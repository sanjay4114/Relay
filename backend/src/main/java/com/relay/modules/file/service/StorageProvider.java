package com.relay.modules.file.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageProvider {
    
    /**
     * Stores a file and returns its stored path/identifier.
     */
    String store(MultipartFile file, String storedName) throws Exception;
    
    /**
     * Stores a generated thumbnail.
     */
    String storeThumbnail(InputStream thumbnailStream, String thumbnailName) throws Exception;
    
    /**
     * Loads a file as a Resource for downloading.
     */
    Resource load(String storagePath) throws Exception;
    
    /**
     * Deletes a file.
     */
    void delete(String storagePath) throws Exception;
    
    /**
     * Provider identifier (e.g. "LOCAL", "S3", "MINIO").
     */
    String getProviderName();
}
