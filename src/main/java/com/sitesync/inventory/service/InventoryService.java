package com.sitesync.inventory.service;

import com.sitesync.inventory.dto.StockLedgerEntryResponse;
import com.sitesync.inventory.dto.StockLevelResponse;
import com.sitesync.inventory.dto.StockRequests.LogConsumptionRequest;
import com.sitesync.inventory.entity.StockLedgerEntry;
import com.sitesync.inventory.entity.StockTransactionType;
import com.sitesync.inventory.repository.StockLedgerRepository;
import com.sitesync.project.service.ProjectService;
import com.sitesync.site.entity.MaterialUnit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core inventory logic. Stock-in (from a delivered purchase order) and
 * stock-out (manual consumption logged by a supervisor) both funnel
 * through the same append-only ledger - see StockLedgerEntry's doc
 * comment for why there's no separate "current stock" row.
 */
@Service
public class InventoryService {

    private final StockLedgerRepository stockLedgerRepository;
    private final ProjectService projectService;

    public InventoryService(StockLedgerRepository stockLedgerRepository, ProjectService projectService) {
        this.stockLedgerRepository = stockLedgerRepository;
        this.projectService = projectService;
    }

    public List<StockLevelResponse> getCurrentStock(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);

        return stockLedgerRepository.aggregateStockByMaterial(projectId).stream()
                .map(row -> new StockLevelResponse(
                        (String) row[0],
                        ((Number) row[1]).doubleValue(),
                        ((MaterialUnit) row[2]).toWireValue()
                ))
                .collect(Collectors.toList());
    }

    public List<StockLedgerEntryResponse> getLedger(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);

        return stockLedgerRepository.findByProjectIdOrderByTimestampDesc(projectId).stream()
                .map(StockLedgerEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Called internally by DeliveryService when a purchase order is
     * marked Delivered - NOT exposed as its own public HTTP endpoint,
     * since stock-in only ever happens as a side effect of a delivery
     * being confirmed, never as a standalone action.
     */
    public void recordStockIn(
            UUID projectId,
            String materialName,
            double quantity,
            MaterialUnit unit,
            UUID purchaseOrderId,
            UUID loggedByUserId
    ) {
        StockLedgerEntry entry = new StockLedgerEntry();
        entry.setProjectId(projectId);
        entry.setMaterialName(materialName);
        entry.setUnit(unit);
        entry.setQuantityDelta(quantity);
        entry.setTransactionType(StockTransactionType.DELIVERY);
        entry.setReferenceId(purchaseOrderId);
        entry.setLoggedByUserId(loggedByUserId);

        stockLedgerRepository.save(entry);
    }

    /**
     * Supervisor logs material used on site. Validates against current
     * stock first: consumption cannot exceed what's actually on hand,
     * since a negative running total would represent physically
     * impossible inventory and likely indicates a missed/late delivery
     * entry rather than something to silently allow.
     */
    public StockLedgerEntryResponse logConsumption(UUID requesterId, LogConsumptionRequest request) {
        UUID projectId = UUID.fromString(request.projectId);
        projectService.getProjectById(projectId, requesterId);

        double currentStock = stockLedgerRepository.sumQuantityForMaterial(projectId, request.materialName);
        if (request.quantity > currentStock) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot log %.2f %s - only %.2f available for %s.",
                            request.quantity, request.unit, currentStock, request.materialName
                    )
            );
        }

        StockLedgerEntry entry = new StockLedgerEntry();
        entry.setProjectId(projectId);
        entry.setMaterialName(request.materialName);
        entry.setUnit(MaterialUnit.fromWireValue(request.unit));
        entry.setQuantityDelta(-Math.abs(request.quantity));
        entry.setTransactionType(StockTransactionType.CONSUMPTION);
        entry.setNotes(request.notes);
        entry.setLoggedByUserId(requesterId);

        StockLedgerEntry saved = stockLedgerRepository.save(entry);
        return StockLedgerEntryResponse.fromEntity(saved);
    }
}