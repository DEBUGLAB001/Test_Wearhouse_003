package com.shopsphere;

import com.shopsphere.dto.PageResponse;
import com.shopsphere.dto.ProductDto;
import com.shopsphere.entity.Category;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CategoryRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.service.ProductService;
import com.shopsphere.util.PaginationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Test 1: Price Filter Upper Boundary
     * BUG 1: Products priced exactly at maxPrice (e.g. ₹5000.0) are omitted because of strictly exclusive `< maxPrice`.
     */
    @Test
    @DisplayName("Bug 1 Test: Price filter inclusive upper boundary")
    public void testPriceFilterInclusiveUpperBound() {
        // Ensure a product priced exactly at 5000.0 exists
        List<Product> boundaryProducts = productRepository.findByActiveTrue().stream()
                .filter(p -> p.getPrice() != null && Math.abs(p.getPrice() - 5000.0) < 0.001)
                .toList();

        assertFalse(boundaryProducts.isEmpty(), "Precondition: A product priced at exactly 5000.0 must exist in seed data");

        // Filter between 1000.0 and 5000.0
        List<Product> filtered = productService.filterProducts(1000.0, 5000.0, null, null, null);

        // Under Bug 1, none of the ₹5000 products will be present in filtered
        boolean contains5000Product = filtered.stream()
                .anyMatch(p -> Math.abs(p.getPrice() - 5000.0) < 0.001);

        assertTrue(contains5000Product, "Expected product priced at exactly ₹5000.0 to be included in price filter [1000, 5000]");
    }

    /**
     * Test 2: Price Filter Lower Bound
     */
    @Test
    public void testPriceFilterLowerBound() {
        List<Product> filtered = productService.filterProducts(1000.0, 6000.0, null, null, null);
        for (Product p : filtered) {
            assertTrue(p.getPrice() >= 1000.0, "Product price must be >= 1000.0");
        }
    }

    /**
     * Test 3: Pagination Exact Page Size
     * BUG 2: Slicing sublist with `fromIndex + pageSize - 1` yields 19 items instead of 20.
     */
    @Test
    @DisplayName("Bug 2 Test: Pagination slice returns exactly 20 items")
    public void testPaginationExactPageSize() {
        List<Integer> mockItems = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            mockItems.add(i);
        }

        PageResponse<Integer> page = PaginationUtil.paginate(mockItems, 0, 20);

        assertEquals(20, page.getContent().size(), "Page size of 20 on 50 items must return exactly 20 items on page 0");
        assertEquals(1, page.getContent().get(0));
        assertEquals(20, page.getContent().get(19), "The 20th item must not be skipped by pagination");
    }

    /**
     * Test 4: Product Creation and Retrieval
     */
    @Test
    public void testProductCreationAndRetrieval() {
        ProductDto dto = new ProductDto();
        dto.setName("Test Wireless Mouse");
        dto.setBrand("TestBrand");
        dto.setPrice(1499.0);
        dto.setRating(4.6);
        dto.setStockQuantity(50);
        dto.setSku("SKU-TEST-001");

        Product created = productService.createProduct(dto);
        assertNotNull(created.getId());

        Optional<Product> retrieved = productService.getProductById(created.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Test Wireless Mouse", retrieved.get().getName());
        assertEquals(1499.0, retrieved.get().getPrice());
    }

    /**
     * Test 5: Product Update and Deletion
     */
    @Test
    public void testProductUpdateAndDeletion() {
        ProductDto dto = new ProductDto();
        dto.setName("Temporary Keyboard");
        dto.setBrand("TempBrand");
        dto.setPrice(799.0);
        dto.setStockQuantity(10);
        dto.setSku("SKU-TEMP-001");

        Product created = productService.createProduct(dto);
        Long id = created.getId();

        dto.setName("Updated Keyboard");
        dto.setPrice(899.0);
        Product updated = productService.updateProduct(id, dto);
        assertEquals("Updated Keyboard", updated.getName());
        assertEquals(899.0, updated.getPrice());

        productService.deleteProduct(id);
        Optional<Product> afterDelete = productService.getProductById(id);
        assertTrue(afterDelete.isEmpty());
    }

    /**
     * Test 6: Category and Brand Filter
     */
    @Test
    public void testCategoryAndBrandFilter() {
        List<Product> appleProducts = productService.filterProducts(null, null, null, "Apple", null);
        assertFalse(appleProducts.isEmpty());
        for (Product p : appleProducts) {
            assertEquals("Apple", p.getBrand());
        }
    }
}
