package com.shopsphere.algorithm;

import com.shopsphere.entity.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Priority queue for order fulfillment dispatch.
 * Express and high-priority orders must be processed and dispatched before standard orders.
 */
@Component
public class OrderPriorityQueue {

    // BUG 3: Comparator compares o1 to o2 ascending (Min-Heap).
    // This causes STANDARD priority orders (level 1) to be polled before EXPRESS (level 3).
    // BUG 3: Comparator compares o1 to o2 ascending (Min-Heap).
    // This causes STANDARD priority orders (level 1) to be polled before EXPRESS (level 3).
    // Fix: (o1, o2) -> Integer.compare(o2.getPriority().getLevel(), o1.getPriority().getLevel())
    private final PriorityQueue<Order> queue = new PriorityQueue<>(
            (o1, o2) -> Integer.compare(o1.getPriority().getLevel(), o2.getPriority().getLevel())
    );

    public synchronized void enqueue(Order order) {
        if (order != null) {
            queue.offer(order);
        }
    }

    public synchronized Order pollNextOrder() {
        return queue.poll();
    }

    public synchronized Order peekNextOrder() {
        return queue.peek();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void clear() {
        queue.clear();
    }

    public synchronized List<Order> toList() {
        return new ArrayList<>(queue);
    }
}
