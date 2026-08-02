package com.sitesync.inventory.dto;

/** One row of the "current stock" view - a material and its live quantity at a project. */
public class StockLevelResponse {
    public String materialName;
    public double currentStock;
    public String unit;

    public StockLevelResponse(String materialName, double currentStock, String unit) {
        this.materialName = materialName;
        this.currentStock = currentStock;
        this.unit = unit;
    }
}