package com.sitesync.delivery.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.delivery.dto.PurchaseOrderRequests.CreatePurchaseOrderRequest;
import com.sitesync.delivery.dto.PurchaseOrderResponse;
import com.sitesync.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getOrders(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<PurchaseOrderResponse> orders = deliveryService.getOrdersForProject(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            Authentication authentication
    ) {
        PurchaseOrderResponse order = deliveryService.createOrder(currentUserId(authentication), request);
        return ResponseEntity.ok(ApiResponse.ok(order, "Delivery scheduled."));
    }

    /** Always multipart - a delivery photo is required, never optional, for this endpoint. */
    @PostMapping(value = "/{id}/deliver", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> markDelivered(
            @PathVariable String id,
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication
    ) {
        PurchaseOrderResponse order = deliveryService.markDelivered(
                UUID.fromString(id), currentUserId(authentication), photo);
        return ResponseEntity.ok(ApiResponse.ok(order, "Delivery confirmed."));
    }
}