package com.shopsphere;

import com.shopsphere.dto.RestockRequest;
import com.shopsphere.entity.InventoryItem;
import com.shopsphere.entity.Product;
import com.shopsphere.entity.Warehouse;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.WarehouseRepository;
import com.shopsphere.service.WarehouseService;
import com.shopsphere.util.InventorySearchUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InventorySearchTests {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    /**
     * Test 1: Binary Search Finds Existing Product (Bug 4)
     * BUG 4: The binary search boundary condition `low < high` terminates before inspecting
     * the last remaining candidate when low == high, returning "Not Found" for existing items.
     */
    @Test
    @DisplayName("Bug 4 Test: Binary search finds every existing product in O(log n)")
    public void testBinarySearchFindsExistingProduct() {
        List<InventoryItem> items = new ArrayList<>();
        // Create 5 items with IDs: 10, 20, 30, 40, 50
        for (long id = 10; id <= 50; id += 10) {
            Product p = new Product();
            p.setId(id);
            p.setName("Product " + id);
            items.add(new InventoryItem(id, p, null, "A1", "B1", 100, 10));
        }

        // Searching for product 50 (at index 4)
        // Under Bug 4 with `low < high`, search terminates at low == high == 4 and fails!
        Optional<InventoryItem> found50 = InventorySearchUtil.binarySearchByProductId(items, 50L);
        assertTrue(found50.isPresent(), "Binary search must locate product ID 50");
        assertEquals(50L, found50.get().getProduct().getId());

        // Searching for product 20 (at index 1)
        Optional<InventoryItem> found20 = InventorySearchUtil.binarySearchByProductId(items, 20L);
        assertTrue(found20.isPresent(), "Binary search must locate product ID 20");
        assertEquals(20L, found20.get().getProduct().getId());
    }

    /**
     * Test 2: Binary Search Finds Boundary Elements (Bug 4)
     */
    @Test
    @DisplayName("Bug 4 Test: Binary search boundary elements (first, last, single-element)")
    public void testBinarySearchFindsBoundaryElements() {
        // Single element list test: low=0, high=0. With low < high, loop never runs!
        Product singleProd = new Product();
        singleProd.setId(100L);
        List<InventoryItem> singleItemList = List.of(new InventoryItem(1L, singleProd, null, "A1", "B1", 50, 10));

        Optional<InventoryItem> singleFound = InventorySearchUtil.binarySearchByProductId(singleItemList, 100L);
        assertTrue(singleFound.isPresent(), "Binary search must find target in single-element list");

        // Multi-element boundary elements
        List<InventoryItem> list = new ArrayList<>();
        for (long id = 1; id <= 10; id++) {
            Product p = new Product();
            p.setId(id);
            list.add(new InventoryItem(id, p, null, "A1", "B1", 50, 10));
        }

        Optional<InventoryItem> first = InventorySearchUtil.binarySearchByProductId(list, 1L);
        assertTrue(first.isPresent(), "Binary search must locate first element (ID 1)");

        Optional<InventoryItem> last = InventorySearchUtil.binarySearchByProductId(list, 10L);
        assertTrue(last.isPresent(), "Binary search must locate last element (ID 10)");
    }

    /**
     * Test 3: Missing Product Returns Not Found
     */
    @Test
    public void testBinarySearchMissingProductReturnsNotFound() {
        List<InventoryItem> items = new ArrayList<>();
        for (long id = 10; id <= 50; id += 10) {
            Product p = new Product();
            p.setId(id);
            items.add(new InventoryItem(id, p, null, "A1", "B1", 100, 10));
        }

        Optional<InventoryItem> missing = InventorySearchUtil.binarySearchByProductId(items, 99999L);
        assertTrue(missing.isEmpty(), "Binary search must return empty Optional for missing product");
    }

    /**
     * Test 4: Restock Increases Quantity
     */
    @Test
    public void testRestockIncreasesQuantity() {
        Product prod = productRepository.findByActiveTrue().get(0);
        Warehouse wh = warehouseRepository.findAll().get(0);

        int initialQty = prod.getStockQuantity();

        RestockRequest request = new RestockRequest(prod.getId(), wh.getId(), 25, "Aisle-9", "Bin-Z-1");
        InventoryItem item = warehouseService.restock(request);

        assertNotNull(item);
        assertTrue(item.getQuantity() >= 25);

        Product updatedProd = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals(initialQty + 25, updatedProd.getStockQuantity());
    }

    /**
     * Test 5: Low Stock Alert Trigger
     */
    @Test
    public void testLowStockAlertTrigger() {
        List<InventoryItem> lowStock = warehouseService.getLowStockAlerts();
        assertNotNull(lowStock);
        for (InventoryItem item : lowStock) {
            assertTrue(item.getQuantity() <= item.getMinThreshold(),
                    "Quantity " + item.getQuantity() + " must be <= threshold " + item.getMinThreshold());
        }
    }

    /**
     * Test 6: Inventory Location Lookup
     */
    @Test
    public void testInventoryLocationLookup() {
        Warehouse wh = warehouseRepository.findAll().get(0);
        List<InventoryItem> warehouseItems = warehouseService.getInventoryByWarehouse(wh.getId());
        assertNotNull(warehouseItems);
        assertFalse(warehouseItems.isEmpty());
        for (InventoryItem item : warehouseItems) {
            assertNotNull(item.getAisle());
            assertNotNull(item.getBin());
            assertEquals(wh.getId(), item.getWarehouse().getId());
        }
    }
}
