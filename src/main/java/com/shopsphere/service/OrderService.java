package com.shopsphere.service;

import com.shopsphere.algorithm.OrderPriorityQueue;
import com.shopsphere.dto.OrderItemDto;
import com.shopsphere.dto.OrderRequest;
import com.shopsphere.entity.*;
import com.shopsphere.repository.CustomerRepository;
import com.shopsphere.repository.OrderRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderPriorityQueue orderPriorityQueue;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        WarehouseRepository warehouseRepository,
                        OrderPriorityQueue orderPriorityQueue) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.orderPriorityQueue = orderPriorityQueue;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    public Order createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPriority(request.getPriority() != null ? request.getPriority() : OrderPriority.STANDARD);
        order.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : customer.getAddress());
        order.setTargetNodeId(request.getTargetNodeId() != null ? request.getTargetNodeId() : customer.getDeliveryNodeId());

        // Select default warehouse if available
        List<Warehouse> warehouses = warehouseRepository.findAll();
        if (!warehouses.isEmpty()) {
            order.setAssignedWarehouse(warehouses.get(0));
        }

        double totalAmount = 0.0;
        if (request.getItems() != null) {
            for (OrderItemDto itemDto : request.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemDto.getProductId()));

                double unitPrice = product.getPrice();
                int qty = itemDto.getQuantity() != null ? itemDto.getQuantity() : 1;
                double subtotal = unitPrice * qty;

                OrderItem orderItem = new OrderItem(null, order, product, qty, unitPrice, subtotal);
                order.addItem(orderItem);
                totalAmount += subtotal;
            }
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Enqueue into fulfillment queue
        orderPriorityQueue.enqueue(savedOrder);

        return savedOrder;
    }

    public Order cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel an order that has already shipped or delivered.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order processNextFulfillmentOrder() {
        Order nextOrder = orderPriorityQueue.pollNextOrder();
        if (nextOrder != null) {
            nextOrder.setStatus(OrderStatus.PROCESSING);
            return orderRepository.save(nextOrder);
        }
        return null;
    }

    public OrderPriorityQueue getFulfillmentQueue() {
        return orderPriorityQueue;
    }
}
