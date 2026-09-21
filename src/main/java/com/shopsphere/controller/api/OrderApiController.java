package com.shopsphere.controller.api;

import com.shopsphere.dto.OrderRequest;
import com.shopsphere.entity.Order;
import com.shopsphere.entity.OrderStatus;
import com.shopsphere.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order cancelled = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelled);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/fulfillment/process-next")
    public ResponseEntity<Order> processNextOrder() {
        Order processed = orderService.processNextFulfillmentOrder();
        if (processed != null) {
            return ResponseEntity.ok(processed);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fulfillment/queue")
    public ResponseEntity<List<Order>> getFulfillmentQueue() {
        return ResponseEntity.ok(orderService.getFulfillmentQueue().toList());
    }
}
