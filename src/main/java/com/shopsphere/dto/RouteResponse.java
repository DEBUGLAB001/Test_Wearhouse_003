package com.shopsphere.dto;

import java.util.List;

public class RouteResponse {
    private String sourceNode;
    private String targetNode;
    private Double totalDistanceKm;
    private List<String> path;
    private Long warehouseId;
    private String warehouseName;

    public RouteResponse() {
    }

    public RouteResponse(String sourceNode, String targetNode, Double totalDistanceKm, List<String> path, Long warehouseId, String warehouseName) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.totalDistanceKm = totalDistanceKm;
        this.path = path;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }
}
