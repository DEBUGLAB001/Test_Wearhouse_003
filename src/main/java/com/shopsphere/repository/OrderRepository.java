package com.shopsphere.repository;

import com.shopsphere.entity.Order;
import com.shopsphere.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != com.shopsphere.entity.OrderStatus.CANCELLED")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.shopsphere.entity.OrderStatus.PENDING")
    Long countPendingOrders();
}
