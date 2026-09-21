package com.shopsphere.controller.web;

import com.shopsphere.entity.InventoryItem;
import com.shopsphere.service.WarehouseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class InventoryWebController {

    private final WarehouseService warehouseService;

    public InventoryWebController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/inventory")
    public String inventory(@RequestParam(required = false) Long searchProductId, Model model) {
        model.addAttribute("warehouses", warehouseService.getAllWarehouses());
        model.addAttribute("lowStockItems", warehouseService.getLowStockAlerts());

        if (searchProductId != null) {
            Optional<InventoryItem> found = warehouseService.searchInventoryByProductId(searchProductId);
            model.addAttribute("searchResult", found.orElse(null));
            model.addAttribute("searched", true);
            model.addAttribute("searchProductId", searchProductId);
        }

        return "inventory";
    }
}
