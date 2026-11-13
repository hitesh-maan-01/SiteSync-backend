package com.sitesync.project.repository;

import com.sitesync.project.entity.ProjectSupervisor;
import com.sitesync.project.entity.ProjectSupervisorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectSupervisorRepository
        extends JpaRepository<ProjectSupervisor, ProjectSupervisorId> {

    List<ProjectSupervisor> findByIdProjectId(UUID projectId);
    List<ProjectSupervisor> findByIdUserId(UUID userId);

    boolean existsByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    void deleteByIdProjectIdAndIdUserId(UUID projectId, UUID userId);

    @Query("""
        SELECT ps.id.projectId FROM ProjectSupervisor ps
        WHERE ps.id.userId = :userId
        """)
    List<UUID> findProjectIdsByUserId(@Param("userId") UUID userId);
}