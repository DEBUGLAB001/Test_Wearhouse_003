package com.shopsphere.controller.web;

import com.shopsphere.dto.PageResponse;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CategoryRepository;
import com.shopsphere.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductWebController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductWebController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minRating,
            Model model) {

        PageResponse<Product> productPage = productService.getPaginatedProducts(page, size, minPrice, maxPrice, categoryId, brand, minRating);

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("minRating", minRating);

        return "products";
    }
}
