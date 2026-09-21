package com.shopsphere.controller.web;

import com.shopsphere.entity.Product;
import com.shopsphere.service.ProductService;
import com.shopsphere.service.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RecommendationWebController {

    private final RecommendationService recommendationService;
    private final ProductService productService;

    public RecommendationWebController(RecommendationService recommendationService, ProductService productService) {
        this.recommendationService = recommendationService;
        this.productService = productService;
    }

    @GetMapping("/recommendations")
    public String recommendations(@RequestParam(required = false) Long productId, Model model) {
        model.addAttribute("products", productService.getAllActiveProducts().stream().limit(50).toList());
        model.addAttribute("selectedProductId", productId);

        if (productId != null) {
            Product baseProduct = productService.getProductById(productId).orElse(null);
            model.addAttribute("baseProduct", baseProduct);

            List<Product> recommended = recommendationService.getRecommendations(productId, 6);
            model.addAttribute("recommendations", recommended);
        }

        return "recommendations";
    }
}
