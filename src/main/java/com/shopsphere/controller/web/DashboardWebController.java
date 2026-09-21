package com.shopsphere.controller.web;

import com.shopsphere.dto.AnalyticsSummaryDto;
import com.shopsphere.service.AnalyticsService;
import com.shopsphere.service.OrderService;
import com.shopsphere.service.WarehouseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardWebController {

    private final AnalyticsService analyticsService;
    private final OrderService orderService;
    private final WarehouseService warehouseService;

    public DashboardWebController(AnalyticsService analyticsService,
                                  OrderService orderService,
                                  WarehouseService warehouseService) {
        this.analyticsService = analyticsService;
        this.orderService = orderService;
        this.warehouseService = warehouseService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        AnalyticsSummaryDto summary = analyticsService.getDashboardSummary();
        model.addAttribute("summary", summary);
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(10).toList());
        model.addAttribute("lowStockItems", warehouseService.getLowStockAlerts().stream().limit(8).toList());
        model.addAttribute("warehouses", warehouseService.getAllWarehouses());
        return "dashboard";
    }
}
