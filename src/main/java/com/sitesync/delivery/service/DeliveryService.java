package com.sitesync.delivery.service;

import com.sitesync.delivery.dto.PurchaseOrderRequests.CreatePurchaseOrderRequest;
import com.sitesync.delivery.dto.PurchaseOrderResponse;
import com.sitesync.delivery.entity.PurchaseOrder;
import com.sitesync.delivery.entity.PurchaseOrderStatus;
import com.sitesync.delivery.repository.PurchaseOrderRepository;
import com.sitesync.inventory.service.InventoryService;
import com.sitesync.project.service.ProjectService;
import com.sitesync.site.entity.Material;
import com.sitesync.site.entity.MaterialUnit;
import com.sitesync.site.repository.MaterialRepository;
import com.sitesync.site.service.FileStorageService;
import com.sitesync.vendor.entity.Vendor;
import com.sitesync.vendor.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orchestrates the purchase-order lifecycle: schedule -> (pending ->
 * late, via OverdueDeliveryScheduler) -> delivered. Marking an order
 * Delivered has two side effects, both performed in the same transaction
 * so they can never partially succeed:
 *   1. A Material row is created in the existing site module (the actual
 *      delivery log entry).
 *   2. A stock-in ledger entry is recorded via InventoryService.
 */
@Service
public class DeliveryService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;
    private final InventoryService inventoryService;
    private final ProjectService projectService;

    public DeliveryService(
            PurchaseOrderRepository purchaseOrderRepository,
            VendorRepository vendorRepository,
            MaterialRepository materialRepository,
            FileStorageService fileStorageService,
            InventoryService inventoryService,
            ProjectService projectService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorRepository = vendorRepository;
        this.materialRepository = materialRepository;
        this.fileStorageService = fileStorageService;
        this.inventoryService = inventoryService;
        this.projectService = projectService;
    }

    public List<PurchaseOrderResponse> getOrdersForProject(UUID projectId, UUID requesterId) {
        projectService.getProjectById(projectId, requesterId);
        return purchaseOrderRepository.findByProjectIdOrderByExpectedAtDesc(projectId).stream()
                .map(PurchaseOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PurchaseOrderResponse createOrder(UUID requesterId, CreatePurchaseOrderRequest request) {
        UUID projectId = UUID.fromString(request.projectId);
        projectService.getProjectById(projectId, requesterId);

        PurchaseOrder order = new PurchaseOrder();
        order.setProjectId(projectId);
        order.setVendorId(UUID.fromString(request.vendorId));
        order.setMaterialName(request.materialName);
        order.setQuantity(request.quantity);
        order.setUnit(MaterialUnit.fromWireValue(request.unit));
        order.setExpectedAt(Instant.parse(normalizeToInstantString(request.expectedAt)));
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setCreatedByUserId(requesterId);

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return PurchaseOrderResponse.fromEntity(saved);
    }

    /**
     * Marks a pending/late order as Delivered. Photo is required (per
     * spec) - enforced here, not left to a nullable field.
     *
     * @Transactional: if the Material or stock-in write fails partway
     * through, the whole operation rolls back - the order should never
     * end up marked Delivered with only some of its side effects applied.
     */
    @Transactional
    public PurchaseOrderResponse markDelivered(UUID orderId, UUID requesterId, MultipartFile photo) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found."));

        projectService.getProjectById(order.getProjectId(), requesterId);

        if (order.getStatus() == PurchaseOrderStatus.DELIVERED) {
            throw new IllegalArgumentException("This order was already marked as delivered.");
        }
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot mark a cancelled order as delivered.");
        }
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("A delivery photo is required.");
        }

        Vendor vendor = vendorRepository.findById(order.getVendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found."));

        String photoUrl = fileStorageService.store(photo, "deliveries");

        Material material = new Material();
        material.setProjectId(order.getProjectId());
        material.setMaterialName(order.getMaterialName());
        material.setQuantity(order.getQuantity());
        material.setUnit(order.getUnit());
        material.setVendorId(order.getVendorId());
        material.setVendorName(vendor.getName());
        material.setDeliveryPhotoUrl(photoUrl);
        material.setLoggedByUserId(requesterId);
        material.setTimestamp(Instant.now());
        Material savedMaterial = materialRepository.save(material);

        inventoryService.recordStockIn(
                order.getProjectId(),
                order.getMaterialName(),
                order.getQuantity(),
                order.getUnit(),
                order.getId(),
                requesterId
        );

        order.setStatus(PurchaseOrderStatus.DELIVERED);
        order.setDeliveredAt(Instant.now());
        order.setDeliveryPhotoUrl(photoUrl);
        order.setDeliveredMaterialId(savedMaterial.getId());

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return PurchaseOrderResponse.fromEntity(saved);
    }

    /**
     * Flutter sends DateTime.toIso8601String() - may or may not include
     * a 'Z'/offset suffix depending on how the DateTime was constructed.
     * Instant.parse() requires one; this normalizes rather than assuming
     * the client always sends a fully-qualified instant string.
     */
    private String normalizeToInstantString(String isoDateTime) {
        return isoDateTime.endsWith("Z") || isoDateTime.contains("+") ? isoDateTime : isoDateTime + "Z";
    }
}