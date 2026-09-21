package com.shopsphere.dto;

import java.util.Map;

public class AnalyticsSummaryDto {
    private Double totalRevenue;
    private Long totalOrders;
    private Long pendingOrders;
    private Long totalProducts;
    private Long lowStockCount;
    private Long activeWarehouses;
    private Map<String, Long> ordersByStatus;

    public AnalyticsSummaryDto() {
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(Long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public Long getActiveWarehouses() {
        return activeWarehouses;
    }

    public void setActiveWarehouses(Long activeWarehouses) {
        this.activeWarehouses = activeWarehouses;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }
}
