package com.sitesync.site.service;

import com.sitesync.project.service.ProjectService;
import com.sitesync.site.dto.MaterialResponse;
import com.sitesync.site.entity.Material;
import com.sitesync.site.entity.MaterialUnit;
import com.sitesync.site.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;
    private final ProjectService projectService;

    public MaterialService(
            MaterialRepository materialRepository,
            FileStorageService fileStorageService,
            ProjectService projectService
    ) {
        this.materialRepository = materialRepository;
        this.fileStorageService = fileStorageService;
        this.projectService = projectService;
    }

    public List<MaterialResponse> getMaterialsForProject(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);
        return materialRepository.findByProjectIdOrderByTimestampDesc(projectId).stream()
                .map(MaterialResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MaterialResponse logMaterial(
            UUID projectId,
            UUID requesterId,
            String materialName,
            Double quantity,
            String unit,
            UUID vendorId,
            String vendorName,
            Double unitCost,
            String notes,
            UUID loggedByUserId,
            MultipartFile deliveryPhoto
    ) {
        projectService.getProjectById(projectId, requesterId);

        Material material = new Material();
        material.setProjectId(projectId);
        material.setMaterialName(materialName);
        material.setQuantity(quantity);
        material.setUnit(MaterialUnit.fromWireValue(unit));
        material.setVendorId(vendorId);
        material.setVendorName(vendorName);
        material.setUnitCost(unitCost);
        material.setNotes(notes);
        material.setLoggedByUserId(loggedByUserId);
        material.setTimestamp(Instant.now());

        if (deliveryPhoto != null && !deliveryPhoto.isEmpty()) {
            material.setDeliveryPhotoUrl(fileStorageService.store(deliveryPhoto, "materials"));
        }

        Material saved = materialRepository.save(material);
        return MaterialResponse.fromEntity(saved);
    }
}