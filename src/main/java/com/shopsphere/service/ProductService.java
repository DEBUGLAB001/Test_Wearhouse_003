package com.shopsphere.service;

import com.shopsphere.dto.PageResponse;
import com.shopsphere.dto.ProductDto;
import com.shopsphere.entity.Category;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CategoryRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.util.PaginationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id).filter(Product::isActive);
    }

    /**
     * Filters products based on price range, category, brand, and minimum rating.
     *
     * BUG 1: The upper price boundary check uses strict inequality (`< maxPrice`)
     * instead of inclusive inequality (`<= maxPrice`). As a result, products priced
     * exactly at the boundary (e.g., ₹5,000 in a ₹1,000–₹5,000 filter) are excluded.
     *
     * Fix: change `p.getPrice() < maxPrice` to `p.getPrice() <= maxPrice`
     */
    public List<Product> filterProducts(Double minPrice, Double maxPrice, Long categoryId, String brand, Double minRating) {
        return filterProductList(productRepository.findByActiveTrue(), minPrice, maxPrice, categoryId, brand, minRating);
    }

    public List<Product> filterProductList(List<Product> products, Double minPrice, Double maxPrice, Long categoryId, String brand, Double minRating) {
        if (products == null) {
            return Collections.emptyList();
        }
        Stream<Product> stream = products.stream().filter(Product::isActive);

        if (minPrice != null) {
            stream = stream.filter(p -> p.getPrice() != null && p.getPrice() >= minPrice);
        }

        if (maxPrice != null) {
            // BUG 1: Upper price filter is strictly exclusive (<) instead of inclusive (<=).
            // Products priced exactly at maxPrice (e.g. 5000.0) are omitted.
            // Fix: stream = stream.filter(p -> p.getPrice() != null && p.getPrice() <= maxPrice);
            stream = stream.filter(p -> p.getPrice() != null && p.getPrice() < maxPrice);
        }

        if (categoryId != null) {
            stream = stream.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId));
        }

        if (brand != null && !brand.trim().isEmpty()) {
            stream = stream.filter(p -> p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand.trim()));
        }

        if (minRating != null) {
            stream = stream.filter(p -> p.getRating() != null && p.getRating() >= minRating);
        }

        return stream.collect(Collectors.toList());
    }

    public PageResponse<Product> getPaginatedProducts(int page, int size, Double minPrice, Double maxPrice, Long categoryId, String brand, Double minRating) {
        List<Product> filtered = filterProducts(minPrice, maxPrice, categoryId, brand, minRating);
        return PaginationUtil.paginate(filtered, page, size);
    }

    public Product createProduct(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setRating(dto.getRating() != null ? dto.getRating() : 4.0);
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        product.setSku(dto.getSku());
        product.setActive(true);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        if (dto.getRating() != null) {
            product.setRating(dto.getRating());
        }
        if (dto.getStockQuantity() != null) {
            product.setStockQuantity(dto.getStockQuantity());
        }
        if (dto.getSku() != null) {
            product.setSku(dto.getSku());
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }
}
