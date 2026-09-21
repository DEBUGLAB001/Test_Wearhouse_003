package com.shopsphere.controller.api;

import com.shopsphere.dto.RestockRequest;
import com.shopsphere.entity.InventoryItem;
import com.shopsphere.entity.Warehouse;
import com.shopsphere.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class WarehouseApiController {

    private final WarehouseService warehouseService;

    public WarehouseApiController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/warehouse/{id}")
    public ResponseEntity<List<InventoryItem>> getWarehouseInventory(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getInventoryByWarehouse(id));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        return ResponseEntity.ok(warehouseService.getLowStockAlerts());
    }

    @GetMapping("/search")
    public ResponseEntity<InventoryItem> searchInventoryByProductId(@RequestParam Long productId) {
        return warehouseService.searchInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/restock")
    public ResponseEntity<InventoryItem> restock(@RequestBody RestockRequest request) {
        InventoryItem item = warehouseService.restock(request);
        return ResponseEntity.ok(item);
    }
}
