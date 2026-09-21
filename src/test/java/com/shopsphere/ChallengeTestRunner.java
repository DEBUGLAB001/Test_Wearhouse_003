package com.shopsphere;

import com.shopsphere.algorithm.DeliveryRouteGraph;
import com.shopsphere.algorithm.OrderPriorityQueue;
import com.shopsphere.algorithm.RecommendationGraph;
import com.shopsphere.dto.PageResponse;
import com.shopsphere.entity.*;
import com.shopsphere.util.InventorySearchUtil;
import com.shopsphere.util.PaginationUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class ChallengeTestRunner {

    static class BugResult {
        String status; // "passed" or "failed"
        long executionTimeMs;

        BugResult(String status, long executionTimeMs) {
            this.status = status;
            this.executionTimeMs = executionTimeMs;
        }
    }

    public static void main(String[] args) {
        long overallStart = System.currentTimeMillis();

        Map<String, BugResult> results = new LinkedHashMap<>();

        // Test Bug 1: Price Filter Upper Boundary
        results.put("Bug 1: Price Filter Upper Boundary", testBug1());

        // Test Bug 2: Product Pagination Slice
        results.put("Bug 2: Product Pagination Slice", testBug2());

        // Test Bug 3: Order Priority Queue Comparator
        results.put("Bug 3: Order Priority Queue Comparator", testBug3());

        // Test Bug 4: Binary Search Inventory Lookup
        results.put("Bug 4: Binary Search Inventory Lookup", testBug4());

        // Test Bug 5: Recommendation Graph Traversal
        results.put("Bug 5: Recommendation Graph Traversal", testBug5());

        // Test Bug 6: Delivery Route Dijkstra Relaxation
        results.put("Bug 6: Delivery Route Dijkstra Relaxation", testBug6());

        long overallEnd = System.currentTimeMillis();
        long totalExecutionTime = overallEnd - overallStart;

        int passed = 0;
        int failed = 0;
        for (BugResult br : results.values()) {
            if ("passed".equals(br.status)) {
                passed++;
            } else {
                failed++;
            }
        }

        // Print ONLY JSON
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, BugResult> entry : results.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": {\n");
            sb.append("    \"Status\": \"").append(entry.getValue().status).append("\",\n");
            sb.append("    \"Execution time\": \"").append(entry.getValue().executionTimeMs).append("ms\"\n");
            sb.append("  },\n");
        }
        sb.append("  \"Total bugs\": 6,\n");
        sb.append("  \"Passed\": ").append(passed).append(",\n");
        sb.append("  \"Failed\": ").append(failed).append(",\n");
        sb.append("  \"Total Execution time\": \"").append(totalExecutionTime).append("ms\"\n");
        sb.append("}");

        System.out.println(sb.toString());

        // Return non-zero exit code if any bug fails
        if (failed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    // Bug 1: Price Filter Upper Boundary
    private static BugResult testBug1() {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            Product p1 = new Product(1L, "Product 1", "Desc", null, "Brand", 4999.0, 4.5, 10, "SKU1");
            Product p2 = new Product(2L, "Product 2", "Desc", null, "Brand", 5000.0, 4.5, 10, "SKU2");
            List<Product> products = List.of(p1, p2);

            Double maxPrice = 5000.0;
            Double minPrice = 1000.0;

            com.shopsphere.service.ProductService service = new com.shopsphere.service.ProductService(null, null);

            List<Product> filtered = service.filterProductList(products, minPrice, maxPrice, null, null, null);
            boolean contains5000 = filtered.stream().anyMatch(p -> p.getPrice() != null && Math.abs(p.getPrice() - 5000.0) < 0.001);
            success = contains5000;
        } catch (Throwable t) {
            success = false;
        }
        long duration = Math.max(1, System.currentTimeMillis() - start);
        return new BugResult(success ? "passed" : "failed", duration);
    }

    // Bug 2: Product Pagination Slice
    private static BugResult testBug2() {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                list.add(i);
            }
            PageResponse<Integer> page = PaginationUtil.paginate(list, 0, 20);
            // Page size is 20. Under Bug 2, size is 19.
            success = (page.getContent().size() == 20) && (page.getContent().get(19) == 20);
        } catch (Throwable t) {
            success = false;
        }
        long duration = Math.max(1, System.currentTimeMillis() - start);
        return new BugResult(success ? "passed" : "failed", duration);
    }

    // Bug 3: Order Priority Queue Comparator
    private static BugResult testBug3() {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            OrderPriorityQueue queue = new OrderPriorityQueue();
            Order standard = new Order(1L, "ORD-STD", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.STANDARD, 500.0, null, null, null);
            Order express = new Order(2L, "ORD-EXP", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.EXPRESS, 1200.0, null, null, null);

            queue.enqueue(standard);
            queue.enqueue(express);

            Order polled = queue.pollNextOrder();
            success = (polled != null && polled.getPriority() == OrderPriority.EXPRESS);
        } catch (Throwable t) {
            success = false;
        }
        long duration = Math.max(1, System.currentTimeMillis() - start);
        return new BugResult(success ? "passed" : "failed", duration);
    }

    // Bug 4: Binary Search Inventory Lookup
    private static BugResult testBug4() {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            List<InventoryItem> items = new ArrayList<>();
            for (long id = 10; id <= 50; id += 10) {
                Product p = new Product();
                p.setId(id);
                items.add(new InventoryItem(id, p, null, "A1", "B1", 100, 10));
            }
            // Search for product ID 50 (boundary element)
            Optional<InventoryItem> found = InventorySearchUtil.binarySearchByProductId(items, 50L);
            success = found.isPresent() && found.get().getProduct().getId().equals(50L);
        } catch (Throwable t) {
            success = false;
        }
        long duration = Math.max(1, System.currentTimeMillis() - start);
        return new BugResult(success ? "passed" : "failed", duration);
    }

    // Bug 5: Recommendation Graph Traversal
    private static BugResult testBug5() {
        long start = System.currentTimeMillis();
        boolean success = false;
        RecommendationGraph graph = new RecommendationGraph();
        graph.addEdge(101L, 102L);
        graph.addEdge(102L, 103L);
        graph.addEdge(103L, 101L);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Long>> future = executor.submit(() -> graph.findRecommendations(101L, 10));
        try {
            List<Long> result = future.get(100, TimeUnit.MILLISECONDS);
            success = (result != null && result.size() == 2 && result.contains(102L) && result.contains(103L));
        } catch (Throwable t) {
            future.cancel(true);
            success = false;
        } finally {
            executor.shutdownNow();
        }
        long duration = Math.max(1, System.currentTimeMillis() - start);
        return new BugResult(success ? "passed" : "failed", duration);
    }

    // Bug 6: Delivery Route Dijkstra Relaxation
    private static BugResult testBug6() {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            DeliveryRouteGraph graph = new DeliveryRouteGraph();
            graph.addRoad("S", "T", 50.0);
            graph.addRoad("S", "A", 10.0);
            graph.addRoad("A", "T", 15.0);

            DeliveryRouteGraph.PathResult result = graph.findShortestPath("S", "T");
            success = (result != null && Math.abs(result.getTotalDistance() - 25.0) < 0.01 && List.of("S", "A", "T").equals(result.getPath()));
        } catch (Throwable t) {
            success = false;
        }
        long duration = Math.max(1, System.currentTimeMillis() - start);
        return new BugResult(success ? "passed" : "failed", duration);
    }

    private static com.shopsphere.repository.ProductRepository createMockProductRepository(List<Product> products) {
        return (com.shopsphere.repository.ProductRepository) java.lang.reflect.Proxy.newProxyInstance(
                com.shopsphere.repository.ProductRepository.class.getClassLoader(),
                new Class<?>[]{com.shopsphere.repository.ProductRepository.class},
                (proxy, method, methodArgs) -> {
                    if ("findByActiveTrue".equals(method.getName())) {
                        return products;
                    }
                    return null;
                }
        );
    }
}
