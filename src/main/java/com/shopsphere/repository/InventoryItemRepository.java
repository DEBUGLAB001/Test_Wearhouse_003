package com.shopsphere.repository;

import com.shopsphere.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByWarehouseId(Long warehouseId);
    List<InventoryItem> findByProductId(Long productId);
    Optional<InventoryItem> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minThreshold")
    List<InventoryItem> findLowStockItems();

    @Query("SELECT i FROM InventoryItem i ORDER BY i.product.id ASC")
    List<InventoryItem> findAllOrderByProductIdAsc();
}
