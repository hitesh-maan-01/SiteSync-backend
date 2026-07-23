package com.sitesync.site.repository;

import com.sitesync.site.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
    List<Material> findByProjectIdOrderByTimestampDesc(UUID projectId);
}