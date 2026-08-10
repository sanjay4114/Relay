package com.relay.modules.identity.repository;

import com.relay.modules.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByPublicIdAndDeletedAtIsNull(String publicId);

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
