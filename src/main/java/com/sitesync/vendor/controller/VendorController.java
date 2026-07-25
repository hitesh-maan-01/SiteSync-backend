package com.sitesync.vendor.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.vendor.dto.VendorRequests.CreateVendorRequest;
import com.sitesync.vendor.dto.VendorRequests.UpdateVendorRequest;
import com.sitesync.vendor.dto.VendorResponse;
import com.sitesync.vendor.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Every mapping here corresponds exactly to a constant in
 * api_endpoints.dart, checked against vendor_repository.dart. GET and
 * POST both live at /api/vendors - differentiated by HTTP method, same
 * pattern as ProjectController.
 */
@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorResponse>>> getVendors(Authentication authentication) {
        List<VendorResponse> vendors = vendorService.getVendorsForRequester(currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(vendors));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VendorResponse>> createVendor(
            @Valid @RequestBody CreateVendorRequest request,
            Authentication authentication
    ) {
        VendorResponse vendor = vendorService.createVendor(currentUserId(authentication), request);
        return ResponseEntity.ok(ApiResponse.ok(vendor, "Vendor added."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable String id,
            @Valid @RequestBody UpdateVendorRequest request,
            Authentication authentication
    ) {
        VendorResponse vendor = vendorService.updateVendor(
                UUID.fromString(id), currentUserId(authentication), request);
        return ResponseEntity.ok(ApiResponse.ok(vendor, "Vendor updated."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(
            @PathVariable String id,
            Authentication authentication
    ) {
        vendorService.deleteVendor(UUID.fromString(id), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(null, "Vendor removed."));
    }
}