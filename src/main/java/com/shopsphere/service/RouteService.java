package com.shopsphere.service;

import com.shopsphere.algorithm.DeliveryRouteGraph;
import com.shopsphere.dto.RouteResponse;
import com.shopsphere.entity.RoadEdge;
import com.shopsphere.entity.Warehouse;
import com.shopsphere.repository.RoadEdgeRepository;
import com.shopsphere.repository.WarehouseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final DeliveryRouteGraph deliveryRouteGraph;
    private final RoadEdgeRepository roadEdgeRepository;
    private final WarehouseRepository warehouseRepository;

    public RouteService(DeliveryRouteGraph deliveryRouteGraph,
                        RoadEdgeRepository roadEdgeRepository,
                        WarehouseRepository warehouseRepository) {
        this.deliveryRouteGraph = deliveryRouteGraph;
        this.roadEdgeRepository = roadEdgeRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @PostConstruct
    public void init() {
        syncGraphFromRepository();
    }

    public synchronized void syncGraphFromRepository() {
        deliveryRouteGraph.clear();
        List<RoadEdge> edges = roadEdgeRepository.findAll();
        for (RoadEdge edge : edges) {
            deliveryRouteGraph.addRoad(edge.getSourceNode(), edge.getTargetNode(), edge.getDistanceKm());
        }
    }

    public RouteResponse calculateShortestRoute(String sourceNode, String targetNode) {
        DeliveryRouteGraph.PathResult result = deliveryRouteGraph.findShortestPath(sourceNode, targetNode);
        return new RouteResponse(sourceNode, targetNode, result.getTotalDistance(), result.getPath(), null, null);
    }

    /**
     * Identifies the closest warehouse to the target customer node using shortest path calculation.
     */
    public RouteResponse findNearestWarehouse(String targetNode) {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        Warehouse closestWarehouse = null;
        DeliveryRouteGraph.PathResult bestResult = null;

        for (Warehouse wh : warehouses) {
            DeliveryRouteGraph.PathResult result = deliveryRouteGraph.findShortestPath(wh.getLocationNodeId(), targetNode);
            if (!Double.isInfinite(result.getTotalDistance())) {
                if (bestResult == null || result.getTotalDistance() < bestResult.getTotalDistance()) {
                    bestResult = result;
                    closestWarehouse = wh;
                }
            }
        }

        if (closestWarehouse != null && bestResult != null) {
            return new RouteResponse(
                    closestWarehouse.getLocationNodeId(),
                    targetNode,
                    bestResult.getTotalDistance(),
                    bestResult.getPath(),
                    closestWarehouse.getId(),
                    closestWarehouse.getName()
            );
        }

        return new RouteResponse(null, targetNode, Double.POSITIVE_INFINITY, List.of(), null, null);
    }

    public DeliveryRouteGraph getGraph() {
        return deliveryRouteGraph;
    }
}
