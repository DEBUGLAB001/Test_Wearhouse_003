package com.shopsphere.algorithm;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Graph representing product co-purchase and category similarity associations.
 * Used to traverse and generate personalized product recommendations.
 */
@Component
public class RecommendationGraph {

    private final Map<Long, List<Long>> adjacencyList = new ConcurrentHashMap<>();

    public void addEdge(Long productA, Long productB) {
        if (productA == null || productB == null || productA.equals(productB)) {
            return;
        }
        adjacencyList.computeIfAbsent(productA, k -> new ArrayList<>()).add(productB);
        adjacencyList.computeIfAbsent(productB, k -> new ArrayList<>()).add(productA);
    }

    public List<Long> getNeighbors(Long productId) {
        return adjacencyList.getOrDefault(productId, Collections.emptyList());
    }

    /**
     * Traverses the product association graph using BFS to find recommended products.
     *
     * BUG 5: The traversal does not track and check visited nodes.
     * When graph components contain cycles (common in co-purchase networks like A <-> B <-> C <-> A),
     * and the total reachable unique products is fewer than maxRecommendations, the queue
     * perpetually re-enqueues visited nodes, trapping the thread in an infinite loop and freezing
     * the recommendation service.
     *
     * Fix:
     * Set<Long> visited = new HashSet<>();
     * visited.add(productId);
     * ...
     * if (!visited.contains(neighbor) && !neighbor.equals(productId)) {
     *     visited.add(neighbor);
     *     recommendations.add(neighbor);
     *     queue.offer(neighbor);
     * }
     */
    public List<Long> findRecommendations(Long productId, int maxRecommendations) {
        if (productId == null || !adjacencyList.containsKey(productId)) {
            return Collections.emptyList();
        }

        List<Long> recommendations = new ArrayList<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(productId);

        Set<Long> visited = new HashSet<>();
        // BUG 5: Visited set is instantiated but neither checked nor populated during neighbor exploration.
        // In cyclic graphs with fewer unique nodes than maxRecommendations, queue never drains!

        int iterations = 0;
        while (!queue.isEmpty() && recommendations.size() < maxRecommendations) {
            if (++iterations > 5000) {
                throw new IllegalStateException("Infinite loop detected: Recommendation graph traversal entered an endless cycle.");
            }
            Long current = queue.poll();
            List<Long> neighbors = adjacencyList.getOrDefault(current, Collections.emptyList());

            for (Long neighbor : neighbors) {
                // BUG 5: Omits `!visited.contains(neighbor)` check and `visited.add(neighbor)`
                // causing endless re-enqueuing of cyclic neighbors.
                if (!neighbor.equals(productId)) {
                    queue.offer(neighbor);
                    if (!recommendations.contains(neighbor)) {
                        recommendations.add(neighbor);
                    }
                }
            }
        }

        return recommendations;
    }

    public void clear() {
        adjacencyList.clear();
    }
}
