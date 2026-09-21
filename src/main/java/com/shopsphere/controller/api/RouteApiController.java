package com.shopsphere.controller.api;

import com.shopsphere.dto.RouteResponse;
import com.shopsphere.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/routes")
public class RouteApiController {

    private final RouteService routeService;

    public RouteApiController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<RouteResponse> calculateShortestRoute(@RequestParam String source, @RequestParam String target) {
        RouteResponse response = routeService.calculateShortestRoute(source, target);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearest-warehouse")
    public ResponseEntity<RouteResponse> findNearestWarehouse(@RequestParam String targetNode) {
        RouteResponse response = routeService.findNearestWarehouse(targetNode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nodes")
    public ResponseEntity<Set<String>> getAllNodes() {
        return ResponseEntity.ok(routeService.getGraph().getAllNodes());
    }
}
