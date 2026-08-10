package com.relay.modules.file.api.dto;

public record FileAttachmentDto(
        String publicId,
        String originalName,
        String mimeType,
        String extension,
        Long fileSize,
        String url,
        String thumbnailUrl
) {
}
