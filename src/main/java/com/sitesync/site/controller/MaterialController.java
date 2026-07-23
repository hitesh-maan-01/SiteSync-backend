package com.sitesync.site.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.site.dto.MaterialRequests.LogMaterialRequest;
import com.sitesync.site.dto.MaterialResponse;
import com.sitesync.site.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Same content-type-based dispatch pattern as AttendanceController - the
 * multipart handler's field names match Flutter's
 * _buildMultipartWithPhoto(..., photoFieldName: 'deliveryPhoto') call in
 * SiteRepository.logMaterial() exactly.
 */
@RestController
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping("/api/projects/{projectId}/materials")
    public ResponseEntity<ApiResponse<List<MaterialResponse>>> getMaterials(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<MaterialResponse> materials = materialService.getMaterialsForProject(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(materials));
    }

    @PostMapping(value = "/api/materials/log", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MaterialResponse>> logMaterialWithPhoto(
            @RequestParam("projectId") String projectId,
            @RequestParam("materialName") String materialName,
            @RequestParam("quantity") Double quantity,
            @RequestParam("unit") String unit,
            @RequestParam("vendorId") String vendorId,
            @RequestParam("vendorName") String vendorName,
            @RequestParam(value = "unitCost", required = false) Double unitCost,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam("loggedByUserId") String loggedByUserId,
            @RequestParam("deliveryPhoto") MultipartFile deliveryPhoto,
            Authentication authentication
    ) {
        MaterialResponse response = materialService.logMaterial(
                UUID.fromString(projectId),
                currentUserId(authentication),
                materialName, quantity, unit,
                UUID.fromString(vendorId), vendorName, unitCost, notes,
                UUID.fromString(loggedByUserId), deliveryPhoto
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Material logged."));
    }

    @PostMapping(value = "/api/materials/log", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MaterialResponse>> logMaterialNoPhoto(
            @Valid @RequestBody LogMaterialRequest request,
            Authentication authentication
    ) {
        MaterialResponse response = materialService.logMaterial(
                UUID.fromString(request.projectId),
                currentUserId(authentication),
                request.materialName, request.quantity, request.unit,
                UUID.fromString(request.vendorId), request.vendorName, request.unitCost, request.notes,
                UUID.fromString(request.loggedByUserId), null
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Material logged."));
    }
}