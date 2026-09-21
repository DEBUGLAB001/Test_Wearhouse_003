package com.shopsphere.service;

import com.shopsphere.dto.RestockRequest;
import com.shopsphere.entity.InventoryItem;
import com.shopsphere.entity.Product;
import com.shopsphere.entity.Warehouse;
import com.shopsphere.repository.InventoryItemRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.WarehouseRepository;
import com.shopsphere.util.InventorySearchUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            InventoryItemRepository inventoryItemRepository,
                            ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.productRepository = productRepository;
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    public List<InventoryItem> getInventoryByWarehouse(Long warehouseId) {
        return inventoryItemRepository.findByWarehouseId(warehouseId);
    }

    public List<InventoryItem> getLowStockAlerts() {
        return inventoryItemRepository.findLowStockItems();
    }

    /**
     * Searches for an inventory item across the catalog using binary search on product ID.
     * Uses InventorySearchUtil to demonstrate O(log N) inventory lookup.
     */
    public Optional<InventoryItem> searchInventoryByProductId(Long productId) {
        List<InventoryItem> allItems = inventoryItemRepository.findAll();
        // Ensure sorted by product ID for binary search
        allItems.sort(Comparator.comparing(item -> item.getProduct().getId()));
        return InventorySearchUtil.binarySearchByProductId(allItems, productId);
    }

    public InventoryItem restock(RestockRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + request.getWarehouseId()));

        Optional<InventoryItem> existingOpt = inventoryItemRepository.findByProductIdAndWarehouseId(
                request.getProductId(), request.getWarehouseId()
        );

        InventoryItem item;
        if (existingOpt.isPresent()) {
            item = existingOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setLastRestocked(LocalDateTime.now());
            if (request.getAisle() != null) item.setAisle(request.getAisle());
            if (request.getBin() != null) item.setBin(request.getBin());
        } else {
            item = new InventoryItem(
                    null,
                    product,
                    warehouse,
                    request.getAisle() != null ? request.getAisle() : "Aisle-1",
                    request.getBin() != null ? request.getBin() : "Bin-A",
                    request.getQuantity(),
                    10
            );
        }

        // Update product stockQuantity cache
        product.setStockQuantity(product.getStockQuantity() + request.getQuantity());
        productRepository.save(product);

        return inventoryItemRepository.save(item);
    }
}
