package com.shopsphere;

import com.shopsphere.algorithm.DeliveryRouteGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DeliveryRouteTests {

    /**
     * Test 1: Shortest Path Calculation with Dijkstra (Bug 6)
     * BUG 6: Dijkstra relaxation uses `newDist > distances.get(v)` instead of `<`.
     * When an alternative multi-hop route S -> A -> T (10 + 15 = 25 km) is shorter than
     * the direct path S -> T (50 km), the relaxation condition rejects the shorter path
     * and returns the suboptimal longer route.
     */
    @Test
    @DisplayName("Bug 6 Test: Delivery routing always returns the shortest warehouse path")
    public void testDijkstraCalculatesShortestPath() {
        DeliveryRouteGraph graph = new DeliveryRouteGraph();

        // Direct road: S -> T = 50.0 km
        graph.addRoad("S", "T", 50.0);
        // Multi-hop road: S -> A = 10.0 km, A -> T = 15.0 km (Total: 25.0 km)
        graph.addRoad("S", "A", 10.0);
        graph.addRoad("A", "T", 15.0);

        DeliveryRouteGraph.PathResult result = graph.findShortestPath("S", "T");

        assertNotNull(result);
        assertEquals(25.0, result.getTotalDistance(), 0.001,
                "Expected shortest path distance of 25.0 km via waypoint A, but got: " + result.getTotalDistance());
        assertEquals(List.of("S", "A", "T"), result.getPath(),
                "Expected path to traverse [S, A, T] instead of direct longer route");
    }

    /**
     * Test 2: Multi-Hop Delivery Route (Bug 6)
     */
    @Test
    @DisplayName("Bug 6 Test: Multi-hop delivery route optimization across hubs")
    public void testDijkstraMultiHopDeliveryRoute() {
        DeliveryRouteGraph graph = new DeliveryRouteGraph();

        // Network with 4 nodes:
        // WH -> HUB_1 (20 km), HUB_1 -> HUB_2 (30 km), HUB_2 -> DEST (20 km) = 70 km
        // WH -> DEST direct (120 km)
        graph.addRoad("WH", "DEST", 120.0);
        graph.addRoad("WH", "HUB_1", 20.0);
        graph.addRoad("HUB_1", "HUB_2", 30.0);
        graph.addRoad("HUB_2", "DEST", 20.0);

        DeliveryRouteGraph.PathResult result = graph.findShortestPath("WH", "DEST");

        assertEquals(70.0, result.getTotalDistance(), 0.001,
                "Dijkstra must find optimal 70 km route through hubs instead of 120 km direct path");
        assertEquals(List.of("WH", "HUB_1", "HUB_2", "DEST"), result.getPath());
    }

    /**
     * Test 3: Direct Route When Shorter
     */
    @Test
    public void testDirectRouteWhenShorter() {
        DeliveryRouteGraph graph = new DeliveryRouteGraph();
        graph.addRoad("WH_A", "WH_B", 30.0);
        graph.addRoad("WH_A", "HUB_X", 25.0);
        graph.addRoad("HUB_X", "WH_B", 25.0);

        DeliveryRouteGraph.PathResult result = graph.findShortestPath("WH_A", "WH_B");
        assertEquals(30.0, result.getTotalDistance(), 0.001);
    }

    /**
     * Test 4: Graph Connectivity Check
     */
    @Test
    public void testGraphConnectivityCheck() {
        DeliveryRouteGraph graph = new DeliveryRouteGraph();
        graph.addRoad("ISLAND_A", "ISLAND_B", 10.0);
        graph.addRoad("MAINLAND_X", "MAINLAND_Y", 15.0);

        DeliveryRouteGraph.PathResult result = graph.findShortestPath("ISLAND_A", "MAINLAND_X");
        assertTrue(Double.isInfinite(result.getTotalDistance()), "Unreachable target node must have infinite distance");
        assertTrue(result.getPath().isEmpty());
    }

    /**
     * Test 5: Warehouse Assignment Nearest Location
     */
    @Test
    public void testWarehouseAssignmentNearestLocation() {
        DeliveryRouteGraph graph = new DeliveryRouteGraph();
        graph.addRoad("NODE_DELHI", "NODE_GURUGRAM", 30.0);
        graph.addRoad("NODE_MUMBAI", "NODE_GURUGRAM", 1200.0);

        DeliveryRouteGraph.PathResult routeDelhi = graph.findShortestPath("NODE_DELHI", "NODE_GURUGRAM");
        DeliveryRouteGraph.PathResult routeMumbai = graph.findShortestPath("NODE_MUMBAI", "NODE_GURUGRAM");

        assertTrue(routeDelhi.getTotalDistance() < routeMumbai.getTotalDistance(),
                "Delhi must be closer to Gurugram than Mumbai");
    }

    /**
     * Test 6: Same Node Route Has Zero Distance
     */
    @Test
    public void testDeliveryCostCalculation() {
        DeliveryRouteGraph graph = new DeliveryRouteGraph();
        graph.addRoad("NODE_1", "NODE_2", 15.0);

        DeliveryRouteGraph.PathResult selfRoute = graph.findShortestPath("NODE_1", "NODE_1");
        assertEquals(0.0, selfRoute.getTotalDistance(), 0.001);
        assertEquals(List.of("NODE_1"), selfRoute.getPath());
    }
}
