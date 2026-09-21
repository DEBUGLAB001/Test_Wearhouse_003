package com.shopsphere.dto;

public class RestockRequest {
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private String aisle;
    private String bin;

    public RestockRequest() {
    }

    public RestockRequest(Long productId, Long warehouseId, Integer quantity, String aisle, String bin) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.aisle = aisle;
        this.bin = bin;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAisle() {
        return aisle;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }
}
