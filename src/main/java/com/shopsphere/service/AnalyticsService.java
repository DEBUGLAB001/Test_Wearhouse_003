package com.shopsphere.service;

import com.shopsphere.dto.AnalyticsSummaryDto;
import com.shopsphere.entity.OrderStatus;
import com.shopsphere.repository.InventoryItemRepository;
import com.shopsphere.repository.OrderRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseRepository warehouseRepository;

    public AnalyticsService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            InventoryItemRepository inventoryItemRepository,
                            WarehouseRepository warehouseRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public AnalyticsSummaryDto getDashboardSummary() {
        AnalyticsSummaryDto summary = new AnalyticsSummaryDto();

        Double rev = orderRepository.calculateTotalRevenue();
        summary.setTotalRevenue(rev != null ? Math.round(rev * 100.0) / 100.0 : 0.0);
        summary.setTotalOrders(orderRepository.count());
        summary.setPendingOrders(orderRepository.countPendingOrders());
        summary.setTotalProducts(productRepository.count());
        summary.setLowStockCount((long) inventoryItemRepository.findLowStockItems().size());
        summary.setActiveWarehouses(warehouseRepository.count());

        Map<String, Long> statusMap = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.findByStatus(status).size();
            statusMap.put(status.name(), count);
        }
        summary.setOrdersByStatus(statusMap);

        return summary;
    }
}
