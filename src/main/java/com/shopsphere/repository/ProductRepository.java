package com.shopsphere.repository;

import com.shopsphere.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findByBrandIgnoreCaseAndActiveTrue(String brand);
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price >= :minPrice AND p.price <= :maxPrice")
    List<Product> findByPriceBetweenInclusive(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true")
    List<String> findDistinctBrands();
}
