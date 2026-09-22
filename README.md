# ShopSphere Pro — E-Commerce Warehouse & Order Management Platform

## 1. Application Overview
**ShopSphere Pro** is an enterprise-grade warehouse management, inventory tracking, and order fulfillment platform designed for high-throughput modern e-commerce operations.

### Key Capabilities
- **Product Catalog Management**: Dynamic filtering across price bands, brands, categories, and customer satisfaction ratings.
- **Order Fulfillment Telemetry**: Multi-tier priority dispatch queue prioritizing critical and express deliveries.
- **Warehouse Inventory & Bin Locator**: Fast $O(\log N)$ inventory search mapping stock to specific warehouses, aisles, and storage bins.
- **Delivery Route Optimizer**: Shortest path logistics routing between regional warehouses, transit hubs, and customer drop-off points using graph algorithms.
- **Product Recommendation Engine**: Graph-based co-purchase and item affinity clustering.
- **Analytics & Command Dashboard**: Real-time revenue telemetry, stock thresholds, and fulfillment KPIs.
- **Role-Based Authentication**: JWT-secured endpoints for `ADMIN`, `WAREHOUSE_MANAGER`, `DELIVERY_PARTNER`, and `CUSTOMER`.

### Technology Stack
- **Backend**: Java 17+, Spring Boot 3.2.x, Spring Data JPA, Spring Security (JWT)
- **Database**: Embedded H2 Database (`jdbc:h2:mem:shopspheredb`) with deterministic seed data
- **Frontend**: Thymeleaf, HTML5, CSS3 Glassmorphism UI
- **Testing**: JUnit 5, Spring Boot Test, MockMvc

---

## 2. Debugging Challenge

This repository contains **6 realistic logical bugs** reported by operations and warehouse personnel. Three of these issues involve core Data Structures and Algorithms (DSA).

### Bug 1 — Price Filter Excludes Maximum Price
- **User Symptom**: Warehouse managers and customers filtering products within a price range (e.g., ₹1,000–₹5,000) notice that items priced at exactly ₹5,000 never appear in the catalog results.
- **Module**: Product Filtering

### Bug 2 — Pagination Skips Every 20th Product
- **User Symptom**: Browsing the catalog with page size set to 20 only renders 19 products per page. Across sequential pages, every 20th product is omitted from the listings.
- **Module**: Catalog Pagination

### Bug 3 — Order Fulfillment Queue Processes Lowest Priority Orders First (DSA)
- **User Symptom**: Emergency Express delivery orders remain stalled in the fulfillment queue while Standard delivery orders are packed and dispatched ahead of them.
- **Module**: Order Fulfillment Dispatch Queue

### Bug 4 — Warehouse Inventory Search Misses Existing Products (DSA)
- **User Symptom**: Warehouse floor workers searching for inventory items by Product ID frequently receive "Not Found" alerts, even when the product physically exists in the warehouse bins.
- **Module**: Warehouse Inventory Bin Search

### Bug 5 — Shopping Cart Recommendation Engine Enters Infinite Loop (DSA)
- **User Symptom**: Loading the "Recommended Products" section causes the browser tab to hang or freeze indefinitely for certain products with co-purchase history.
- **Module**: Recommendation Engine

### Bug 6 — Delivery Route Calculator Returns Longer Route (DSA)
- **User Symptom**: The delivery route optimizer assigns logistics drivers longer multi-warehouse routes, increasing transit times and fuel consumption despite shorter intermediate paths being available.
- **Module**: Logistics Delivery Routing

---

## 3. Expected Behavior After Fixes
Once all 6 bugs are diagnosed and resolved:
1. Price filtering includes upper boundary products (e.g., ₹5,000 products appear in ₹1,000–₹5,000 filters).
2. Pagination displays exactly 20 items per full page without skipping items.
3. Express delivery orders are dispatched before standard orders in the fulfillment queue.
4. Binary search inventory lookups successfully locate every existing product in $O(\log N)$ time, including boundary products.
5. Product recommendation graph traversal handles cyclic co-purchase graphs smoothly without freezing.
6. Logistics delivery routing calculates the shortest transit distance across regional hubs.
7. The automated challenge test runner reports all 6 tests as `passed` with exit code 0.

---