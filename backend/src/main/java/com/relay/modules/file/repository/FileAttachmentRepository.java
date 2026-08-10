package com.relay.modules.file.repository;

import com.relay.modules.file.domain.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    Optional<FileAttachment> findByPublicId(String publicId);
    Optional<FileAttachment> findByChecksum(String checksum);
}
