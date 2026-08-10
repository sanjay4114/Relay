package com.relay.modules.file.api;

import com.relay.common.dto.ApiResponse;
import com.relay.modules.file.domain.FileAttachment;
import com.relay.modules.file.service.FileService;
import com.relay.config.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser user) {
            
        FileAttachment attachment = fileService.uploadFile(file, user.userId());
        
        return ApiResponse.ok(Map.of(
                "publicId", attachment.getPublicId(),
                "originalName", attachment.getOriginalName(),
                "mimeType", attachment.getMimeType(),
                "size", String.valueOf(attachment.getFileSize()),
                "url", "/api/v1/files/" + attachment.getPublicId() + "/download",
                "thumbnailUrl", attachment.getThumbnailPath() != null ? "/api/v1/files/" + attachment.getPublicId() + "/thumbnail" : null
        ), "File uploaded successfully");
    }

    @GetMapping("/{publicId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String publicId) {
        Resource resource = fileService.loadFileAsResource(publicId);
        
        // Let's resolve content type dynamically or default to octet-stream
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @GetMapping("/{publicId}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String publicId) {
        Resource resource = fileService.loadThumbnailAsResource(publicId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
