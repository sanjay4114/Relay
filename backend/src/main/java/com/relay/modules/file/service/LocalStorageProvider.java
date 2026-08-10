package com.relay.modules.file.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalStorageProvider implements StorageProvider {

    private final Path rootLocation;

    public LocalStorageProvider(@Value("${relay.storage.local.dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(MultipartFile file, String storedName) throws Exception {
        Path destinationFile = this.rootLocation.resolve(Paths.get(storedName))
                .normalize().toAbsolutePath();
        
        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new SecurityException("Cannot store file outside current directory.");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return storedName;
    }

    @Override
    public String storeThumbnail(InputStream thumbnailStream, String thumbnailName) throws Exception {
        Path destinationFile = this.rootLocation.resolve(Paths.get(thumbnailName))
                .normalize().toAbsolutePath();
                
        Files.copy(thumbnailStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        
        return thumbnailName;
    }

    @Override
    public Resource load(String storagePath) throws Exception {
        Path file = rootLocation.resolve(storagePath);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read file: " + storagePath);
        }
    }

    @Override
    public void delete(String storagePath) throws Exception {
        Path file = rootLocation.resolve(storagePath);
        Files.deleteIfExists(file);
    }

    @Override
    public String getProviderName() {
        return "LOCAL";
    }
}
