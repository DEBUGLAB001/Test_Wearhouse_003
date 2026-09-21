package com.shopsphere.controller.api;

import com.shopsphere.entity.Product;
import com.shopsphere.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationApiController {

    private final RecommendationService recommendationService;

    public RecommendationApiController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<Product>> getRecommendations(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "6") int limit) {
        List<Product> products = recommendationService.getRecommendations(productId, limit);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/associate")
    public ResponseEntity<Void> associateProducts(@RequestParam Long productA, @RequestParam Long productB) {
        recommendationService.addCoPurchaseEdge(productA, productB);
        return ResponseEntity.ok().build();
    }
}
