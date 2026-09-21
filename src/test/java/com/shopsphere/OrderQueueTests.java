package com.shopsphere;

import com.shopsphere.algorithm.OrderPriorityQueue;
import com.shopsphere.dto.OrderItemDto;
import com.shopsphere.dto.OrderRequest;
import com.shopsphere.entity.*;
import com.shopsphere.repository.CustomerRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderQueueTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Test 1: Express Order Processed Before Standard (Bug 3)
     * BUG 3: PriorityQueue comparator sorts ascending, making STANDARD (level 1)
     * poll before EXPRESS (level 3).
     */
    @Test
    @DisplayName("Bug 3 Test: Express delivery orders ship before normal orders")
    public void testExpressOrderProcessedBeforeStandard() {
        OrderPriorityQueue queue = new OrderPriorityQueue();

        Order standardOrder = new Order(1L, "ORD-STD-01", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.STANDARD, 500.0, "Addr 1", "N1", null);
        Order expressOrder = new Order(2L, "ORD-EXP-01", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.EXPRESS, 1200.0, "Addr 2", "N2", null);

        // Enqueue standard first, then express
        queue.enqueue(standardOrder);
        queue.enqueue(expressOrder);

        // Highest priority order (EXPRESS) must be dequeued first
        Order nextDispatched = queue.pollNextOrder();
        assertNotNull(nextDispatched, "Fulfillment queue must not be empty");
        assertEquals(OrderPriority.EXPRESS, nextDispatched.getPriority(),
                "Expected EXPRESS order to be processed before STANDARD order, but got: " + nextDispatched.getPriority());
        assertEquals("ORD-EXP-01", nextDispatched.getOrderNumber());
    }

    /**
     * Test 2: Priority Queue Ordering Multiple Tiers (Bug 3)
     */
    @Test
    @DisplayName("Bug 3 Test: Heap ordering across multiple priority tiers")
    public void testPriorityQueueOrderingMultipleTiers() {
        OrderPriorityQueue queue = new OrderPriorityQueue();

        Order standard = new Order(10L, "ORD-1", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.STANDARD, 100.0, null, null, null);
        Order priority = new Order(20L, "ORD-2", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.PRIORITY, 200.0, null, null, null);
        Order express = new Order(30L, "ORD-3", null, LocalDateTime.now(), OrderStatus.PENDING, OrderPriority.EXPRESS, 300.0, null, null, null);

        queue.enqueue(standard);
        queue.enqueue(priority);
        queue.enqueue(express);

        assertEquals(OrderPriority.EXPRESS, queue.pollNextOrder().getPriority(), "First order must be EXPRESS");
        assertEquals(OrderPriority.PRIORITY, queue.pollNextOrder().getPriority(), "Second order must be PRIORITY");
        assertEquals(OrderPriority.STANDARD, queue.pollNextOrder().getPriority(), "Third order must be STANDARD");
    }

    /**
     * Test 3: Order Placement
     */
    @Test
    public void testOrderPlacement() {
        Customer cust = customerRepository.findAll().get(0);
        Product prod = productRepository.findByActiveTrue().get(0);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(cust.getId());
        request.setPriority(OrderPriority.EXPRESS);
        request.setDeliveryAddress("99 Express Way");
        request.setItems(List.of(new OrderItemDto(prod.getId(), 2)));

        Order created = orderService.createOrder(request);
        assertNotNull(created.getId());
        assertNotNull(created.getOrderNumber());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertEquals(OrderPriority.EXPRESS, created.getPriority());
        assertEquals(prod.getPrice() * 2, created.getTotalAmount(), 0.01);
    }

    /**
     * Test 4: Order Cancellation
     */
    @Test
    public void testOrderCancellation() {
        Customer cust = customerRepository.findAll().get(0);
        Product prod = productRepository.findByActiveTrue().get(0);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(cust.getId());
        request.setPriority(OrderPriority.STANDARD);
        request.setItems(List.of(new OrderItemDto(prod.getId(), 1)));

        Order created = orderService.createOrder(request);
        Order cancelled = orderService.cancelOrder(created.getId());

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    /**
     * Test 5: Order Status Transition
     */
    @Test
    public void testOrderStatusTransition() {
        Customer cust = customerRepository.findAll().get(0);
        Product prod = productRepository.findByActiveTrue().get(0);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(cust.getId());
        request.setPriority(OrderPriority.PRIORITY);
        request.setItems(List.of(new OrderItemDto(prod.getId(), 1)));

        Order created = orderService.createOrder(request);
        Order updated = orderService.updateOrderStatus(created.getId(), OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
    }

    /**
     * Test 6: Order History Retrieval
     */
    @Test
    public void testOrderHistoryRetrieval() {
        Customer cust = customerRepository.findAll().get(0);
        List<Order> history = orderService.getOrdersByCustomer(cust.getId());
        assertNotNull(history);
        for (Order o : history) {
            assertEquals(cust.getId(), o.getCustomer().getId());
        }
    }
}
