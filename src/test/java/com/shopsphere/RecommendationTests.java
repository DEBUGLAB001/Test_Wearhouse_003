package com.shopsphere;

import com.shopsphere.algorithm.RecommendationGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RecommendationTests {

    /**
     * Test 1: Recommendation Cycle Handling / No Infinite Loop (Bug 5)
     * BUG 5: Graph traversal does not check visited set, so cyclic associations
     * (e.g. A <-> B <-> C <-> A) cause an endless while loop when reachable items < maxCount.
     */
    @Test
    @DisplayName("Bug 5 Test: Recommendation graph traversal never loops infinitely on cycles")
    public void testRecommendationCycleHandlingNoInfiniteLoop() {
        RecommendationGraph graph = new RecommendationGraph();
        // Construct cyclic graph: 101 <-> 102 <-> 103 <-> 101
        graph.addEdge(101L, 102L);
        graph.addEdge(102L, 103L);
        graph.addEdge(103L, 101L);

        // Run with 1000ms timeout to detect infinite loop deterministically
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Long>> future = executor.submit(() -> graph.findRecommendations(101L, 10));

        try {
            List<Long> recs = future.get(1000, TimeUnit.MILLISECONDS);
            assertNotNull(recs);
            assertTrue(recs.contains(102L));
            assertTrue(recs.contains(103L));
            assertFalse(recs.contains(101L), "Starting node should not be in its own recommendations");
            assertEquals(2, recs.size(), "Should return exactly the 2 reachable associated nodes");
        } catch (TimeoutException e) {
            future.cancel(true);
            fail("Recommendation traversal entered an infinite loop due to cyclic graph structure (Bug 5)");
        } catch (Exception e) {
            fail("Unexpected exception during recommendation traversal: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Test 2: Recommendation Returns Co-Purchased Products
     */
    @Test
    public void testRecommendationReturnsCoPurchasedProducts() {
        RecommendationGraph graph = new RecommendationGraph();
        graph.addEdge(1L, 2L);
        graph.addEdge(2L, 3L);

        List<Long> direct = graph.getNeighbors(1L);
        assertTrue(direct.contains(2L));
    }

    /**
     * Test 3: Recommendation Empty for Isolated Product
     */
    @Test
    public void testRecommendationEmptyForIsolatedProduct() {
        RecommendationGraph graph = new RecommendationGraph();
        graph.addEdge(10L, 20L);

        List<Long> result = graph.findRecommendations(999L, 5);
        assertTrue(result.isEmpty(), "Isolated product without associations must return empty list");
    }

    /**
     * Test 4: Recommendation Limit Enforcement
     */
    @Test
    public void testRecommendationDepthLimit() {
        RecommendationGraph graph = new RecommendationGraph();
        for (long i = 2; i <= 20; i++) {
            graph.addEdge(1L, i);
        }

        List<Long> recs = graph.findRecommendations(1L, 5);
        assertTrue(recs.size() <= 5, "Recommendations must respect the limit of 5");
    }

    /**
     * Test 5: Recommendation Graph Add Edge
     */
    @Test
    public void testRecommendationGraphAddEdge() {
        RecommendationGraph graph = new RecommendationGraph();
        graph.addEdge(50L, 60L);

        assertTrue(graph.getNeighbors(50L).contains(60L));
        assertTrue(graph.getNeighbors(60L).contains(50L));
    }

    /**
     * Test 6: Recommendation Direct Neighbors
     */
    @Test
    public void testRecommendationDirectNeighbors() {
        RecommendationGraph graph = new RecommendationGraph();
        graph.addEdge(100L, 200L);
        graph.addEdge(100L, 300L);

        List<Long> neighbors = graph.getNeighbors(100L);
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(200L));
        assertTrue(neighbors.contains(300L));
    }
}
