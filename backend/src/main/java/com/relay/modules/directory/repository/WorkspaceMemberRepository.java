package com.relay.modules.directory.repository;

import com.relay.modules.directory.domain.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    @Query("""
            SELECT wm.workspace FROM WorkspaceMember wm
            WHERE wm.user.id = :userId
            ORDER BY wm.joinedAt ASC
            """)
    List<com.relay.modules.tenant.domain.Workspace> findWorkspacesByUserId(@Param("userId") Long userId);

    default Optional<com.relay.modules.tenant.domain.Workspace> findDefaultWorkspaceForUser(Long userId) {
        List<com.relay.modules.tenant.domain.Workspace> workspaces = findWorkspacesByUserId(userId);
        return workspaces.isEmpty() ? Optional.empty() : Optional.of(workspaces.getFirst());
    }

    @Query("SELECT wm FROM WorkspaceMember wm JOIN FETCH wm.workspace WHERE wm.user.id = :userId")
    List<WorkspaceMember> findAllByUserId(@Param("userId") Long userId);

    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);
    
    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);
    
    Optional<WorkspaceMember> findByWorkspacePublicIdAndUserPublicId(String workspacePublicId, String userPublicId);

    long countByWorkspaceIdAndWorkspaceRole(Long workspaceId, com.relay.modules.directory.domain.WorkspaceRole role);
}
