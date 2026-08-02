package com.sitesync.inventory.repository;

import com.sitesync.inventory.entity.StockLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockLedgerRepository extends JpaRepository<StockLedgerEntry, UUID> {

    List<StockLedgerEntry> findByProjectIdOrderByTimestampDesc(UUID projectId);

    /**
     * One row per distinct material at this project, with its current
     * stock level (SUM of every delta) and the unit last used for it.
     * Returns Object[] (name, currentStock, unit) rather than a
     * projection interface - simplest option for a 3-column aggregate
     * that StockService maps into a proper DTO immediately after the
     * query runs, so the raw shape never leaks past the service.
     *
     * NOTE: assumes a material is always logged with a consistent unit
     * within a project (e.g. always "bag" for cement, never mixed with
     * "kg" for the same material name) - mixing units for the same
     * material name would make the SUM meaningless. This isn't validated
     * anywhere yet; a real constraint or unit-conversion layer would be
     * needed if that assumption doesn't hold in practice.
     */
    @Query("""
        SELECT s.materialName, SUM(s.quantityDelta), MAX(s.unit)
        FROM StockLedgerEntry s
        WHERE s.projectId = :projectId
        GROUP BY s.materialName
        HAVING SUM(s.quantityDelta) <> 0
        ORDER BY s.materialName ASC
        """)
    List<Object[]> aggregateStockByMaterial(@Param("projectId") UUID projectId);

    /** Current stock for one specific material - used to validate a stock-out doesn't exceed what's available. */
    @Query("""
        SELECT COALESCE(SUM(s.quantityDelta), 0)
        FROM StockLedgerEntry s
        WHERE s.projectId = :projectId AND s.materialName = :materialName
        """)
    Double sumQuantityForMaterial(@Param("projectId") UUID projectId, @Param("materialName") String materialName);
}