package com.sitesync.inventory.controller;

import com.sitesync.auth.dto.ApiResponse;
import com.sitesync.inventory.dto.StockLedgerEntryResponse;
import com.sitesync.inventory.dto.StockLevelResponse;
import com.sitesync.inventory.dto.StockRequests.LogConsumptionRequest;
import com.sitesync.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping("/{projectId}/stock")
    public ResponseEntity<ApiResponse<List<StockLevelResponse>>> getCurrentStock(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<StockLevelResponse> stock = inventoryService.getCurrentStock(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(stock));
    }

    @GetMapping("/{projectId}/ledger")
    public ResponseEntity<ApiResponse<List<StockLedgerEntryResponse>>> getLedger(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        List<StockLedgerEntryResponse> ledger = inventoryService.getLedger(
                UUID.fromString(projectId), currentUserId(authentication));
        return ResponseEntity.ok(ApiResponse.ok(ledger));
    }

    @PostMapping("/consume")
    public ResponseEntity<ApiResponse<StockLedgerEntryResponse>> logConsumption(
            @Valid @RequestBody LogConsumptionRequest request,
            Authentication authentication
    ) {
        StockLedgerEntryResponse entry = inventoryService.logConsumption(currentUserId(authentication), request);
        return ResponseEntity.ok(ApiResponse.ok(entry, "Consumption logged."));
    }
}