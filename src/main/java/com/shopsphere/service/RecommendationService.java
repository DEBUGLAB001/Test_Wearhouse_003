package com.shopsphere.service;

import com.shopsphere.algorithm.RecommendationGraph;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecommendationGraph recommendationGraph;
    private final ProductRepository productRepository;

    public RecommendationService(RecommendationGraph recommendationGraph, ProductRepository productRepository) {
        this.recommendationGraph = recommendationGraph;
        this.productRepository = productRepository;
    }

    public void addCoPurchaseEdge(Long productA, Long productB) {
        recommendationGraph.addEdge(productA, productB);
    }

    public List<Product> getRecommendations(Long productId, int maxCount) {
        List<Long> recommendedIds = recommendationGraph.findRecommendations(productId, maxCount);
        if (recommendedIds.isEmpty()) {
            return Collections.emptyList();
        }

        return recommendedIds.stream()
                .map(id -> productRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public RecommendationGraph getGraph() {
        return recommendationGraph;
    }
}
