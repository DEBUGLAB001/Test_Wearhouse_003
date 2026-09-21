package com.shopsphere.controller.web;

import com.shopsphere.dto.RouteResponse;
import com.shopsphere.service.RouteService;
import com.shopsphere.service.WarehouseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoutingWebController {

    private final RouteService routeService;
    private final WarehouseService warehouseService;

    public RoutingWebController(RouteService routeService, WarehouseService warehouseService) {
        this.routeService = routeService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/routing")
    public String routing(
            @RequestParam(required = false) String sourceNode,
            @RequestParam(required = false) String targetNode,
            Model model) {

        model.addAttribute("nodes", routeService.getGraph().getAllNodes());
        model.addAttribute("warehouses", warehouseService.getAllWarehouses());
        model.addAttribute("sourceNode", sourceNode);
        model.addAttribute("targetNode", targetNode);

        if (sourceNode != null && targetNode != null && !sourceNode.trim().isEmpty() && !targetNode.trim().isEmpty()) {
            RouteResponse route = routeService.calculateShortestRoute(sourceNode.trim(), targetNode.trim());
            model.addAttribute("calculatedRoute", route);
        }

        return "routing";
    }
}
