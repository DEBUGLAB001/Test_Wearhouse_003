package com.shopsphere.algorithm;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Weighted graph modeling delivery hubs, warehouses, and customer delivery routes.
 * Employs Dijkstra's algorithm to compute the shortest delivery route.
 */
@Component
public class DeliveryRouteGraph {

    public static class Edge {
        private final String target;
        private final double weight;

        public Edge(String target, double weight) {
            this.target = target;
            this.weight = weight;
        }

        public String getTarget() {
            return target;
        }

        public double getWeight() {
            return weight;
        }
    }

    public static class NodeDistance {
        private final String node;
        private final double distance;

        public NodeDistance(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }

        public String getNode() {
            return node;
        }

        public double getDistance() {
            return distance;
        }
    }

    public static class PathResult {
        private final double totalDistance;
        private final List<String> path;

        public PathResult(double totalDistance, List<String> path) {
            this.totalDistance = totalDistance;
            this.path = path;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public List<String> getPath() {
            return path;
        }
    }

    private final Map<String, List<Edge>> adjacencyMap = new ConcurrentHashMap<>();

    public void addRoad(String source, String destination, double distanceKm) {
        if (source == null || destination == null || distanceKm <= 0) {
            return;
        }
        adjacencyMap.computeIfAbsent(source, k -> new ArrayList<>()).add(new Edge(destination, distanceKm));
        adjacencyMap.computeIfAbsent(destination, k -> new ArrayList<>()).add(new Edge(source, distanceKm));
    }

    public List<Edge> getAdjacentEdges(String node) {
        return adjacencyMap.getOrDefault(node, Collections.emptyList());
    }

    public Set<String> getAllNodes() {
        return adjacencyMap.keySet();
    }

    /**
     * Calculates the shortest delivery path between two nodes using Dijkstra's algorithm.
     *
     * BUG 6: Flawed relaxation condition.
     * The relaxation step checks `newDist > distances.get(v)` instead of `newDist < distances.get(v)`
     * for already discovered nodes. Consequently, once a path discovers a node, any subsequently
     * evaluated shorter alternative paths are rejected, while longer paths overwrite optimal distances,
     * assigning orders longer delivery routes.
     *
     * Fix:
     * if (newDist < distances.get(v)) {
     *     distances.put(v, newDist);
     *     previous.put(v, u);
     *     pq.offer(new NodeDistance(v, newDist));
     * }
     */
    public PathResult findShortestPath(String startNode, String endNode) {
        if (startNode == null || endNode == null || !adjacencyMap.containsKey(startNode) || !adjacencyMap.containsKey(endNode)) {
            return new PathResult(Double.POSITIVE_INFINITY, Collections.emptyList());
        }

        if (startNode.equals(endNode)) {
            return new PathResult(0.0, List.of(startNode));
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        for (String node : adjacencyMap.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(startNode, 0.0);
        pq.offer(new NodeDistance(startNode, 0.0));

        Set<String> settled = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.node;

            if (settled.contains(u)) {
                continue;
            }
            settled.add(u);

            if (u.equals(endNode)) {
                break;
            }

            for (Edge edge : adjacencyMap.getOrDefault(u, Collections.emptyList())) {
                String v = edge.target;
                if (settled.contains(v)) {
                    continue;
                }
                double edgeWeight = edge.weight;
                double newDist = distances.get(u) + edgeWeight;

                // BUG 6: Relaxation comparison is inverted for discovered nodes!
                // Uses `newDist > distances.get(v)` instead of `newDist < distances.get(v)`.
                // Correct relaxation: if (newDist < distances.get(v))
                if (distances.get(v) == Double.POSITIVE_INFINITY || newDist > distances.get(v)) {
                    distances.put(v, newDist);
                    previous.put(v, u);
                    pq.offer(new NodeDistance(v, newDist));
                }
            }
        }

        double totalDist = distances.getOrDefault(endNode, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(totalDist)) {
            return new PathResult(Double.POSITIVE_INFINITY, Collections.emptyList());
        }

        List<String> path = new LinkedList<>();
        for (String at = endNode; at != null; at = previous.get(at)) {
            path.add(0, at);
        }

        return new PathResult(totalDist, path);
    }

    public void clear() {
        adjacencyMap.clear();
    }
}
