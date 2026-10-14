## Description
ShopSphere Pro is an e-commerce warehouse and order management platform featuring product catalog filtering, priority fulfillment dispatching, binary search inventory tracking, Dijkstra delivery route optimization, and graph-based product recommendations. The application is built with Java 17, Spring Boot 3, Spring Data JPA, Spring Security with JWT, and Thymeleaf.

## Repository Structure
```text
shopsphere/
├── challenge.json                                Challenge configuration and test execution commands
├── pom.xml                                       Maven project configuration and Spring Boot dependencies
├── README.md                                     Project documentation and challenge overview
├── src/
│   ├── main/
│   │   ├── java/com/shopsphere/
│   │   │   ├── ShopSphereApplication.java        Spring Boot application entry point
│   │   │   ├── algorithm/
│   │   │   │   ├── DeliveryRouteGraph.java       Weighted delivery graph and Dijkstra shortest path algorithm
│   │   │   │   ├── OrderPriorityQueue.java       Priority queue dispatching orders by fulfillment priority
│   │   │   │   └── RecommendationGraph.java      Graph data structure for co-purchase product recommendations
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java          Database seeder for sample products, warehouses, and orders
│   │   │   │   └── SecurityConfig.java           Spring Security filter chain and JWT authentication config
│   │   │   ├── controller/
│   │   │   │   ├── api/
│   │   │   │   │   ├── AnalyticsApiController.java     REST controller for warehouse metrics and KPIs
│   │   │   │   │   ├── AuthApiController.java          REST controller for authentication and token generation
│   │   │   │   │   ├── OrderApiController.java         REST controller for order management and fulfillment
│   │   │   │   │   ├── ProductApiController.java       REST controller for product catalog and filtering
│   │   │   │   │   ├── RecommendationApiController.java REST controller for co-purchase recommendations
│   │   │   │   │   ├── RouteApiController.java         REST controller for delivery route calculation
│   │   │   │   │   └── WarehouseApiController.java     REST controller for inventory stock management
│   │   │   │   └── web/
│   │   │   │       ├── DashboardWebController.java     Web controller rendering executive metrics dashboard
│   │   │   │       ├── InventoryWebController.java     Web controller rendering warehouse inventory management
│   │   │   │       ├── LoginWebController.java         Web controller rendering authentication login page
│   │   │   │       ├── OrderWebController.java         Web controller rendering order fulfillment queue
│   │   │   │       ├── ProductWebController.java       Web controller rendering product catalog page
│   │   │   │       ├── RecommendationWebController.java Web controller rendering product recommendations page
│   │   │   │       └── RoutingWebController.java       Web controller rendering delivery route calculator
│   │   │   ├── dto/                              Data transfer objects for requests, responses, and pagination
│   │   │   ├── entity/                           JPA entities modeling products, orders, inventory, and warehouses
│   │   │   ├── repository/                       Spring Data JPA repositories for database operations
│   │   │   ├── security/                         JWT token provider and user details authentication services
│   │   │   ├── service/                          Business logic services for catalog, orders, routing, and inventory
│   │   │   └── util/
│   │   │       ├── InventorySearchUtil.java      Binary search utility for inventory product lookup
│   │   │       └── PaginationUtil.java           Utility for in-memory pagination slicing
│   │   └── resources/
│   │       ├── application.yml                   Application configuration and H2 database settings
│   │       ├── static/css/style.css              Glassmorphism UI styling for web interfaces
│   │       └── templates/                        Thymeleaf HTML templates for browser views
│   └── test/java/com/shopsphere/
│       ├── AuthTests.java                        Unit tests for authentication and role authorization
│       ├── ChallengeTestRunner.java              Autonomous runner evaluating the 6 challenge test cases
│       ├── DeliveryRouteTests.java               Unit tests for delivery route shortest path algorithms
│       ├── InventorySearchTests.java             Unit tests for binary search inventory lookup
│       ├── OrderQueueTests.java                  Unit tests for order fulfillment priority queue
│       ├── ProductTests.java                     Unit tests for product filtering and pagination
│       └── RecommendationTests.java              Unit tests for recommendation graph cycle handling
```

## Bugs and Bug Locations

### 1. Price Filter Excludes Upper Price Boundary
- **Bug location:** src/main/java/com/shopsphere/service/ProductService.java
- **How to observe it:** Filter products with a maximum price matching an active product's exact price (e.g., filter between ₹1000.0 and ₹5000.0 when a product priced at ₹5000.0 exists).
- **Failure:** Products priced exactly at the maximum price limit are excluded because the filter condition uses strict less-than (`< maxPrice`).
- **Expected:** Products priced at or below the maximum price boundary should be included in the filtered results using an inclusive comparison (`<= maxPrice`).

### 2. Catalog Pagination Skips Every 20th Product
- **Bug location:** src/main/java/com/shopsphere/util/PaginationUtil.java
- **How to observe it:** Paginate a list of 50 items with `page = 0` and `size = 20`.
- **Failure:** The page response contains only 19 items instead of 20, and the 20th item is omitted because the slice upper bound subtracts 1 (`fromIndex + size - 1`).
- **Expected:** The sublist ending index should be `Math.min(fromIndex + size, totalElements)`, returning all 20 items per full page without omitting items.

### 3. Fulfillment Queue Processes Lowest Priority Orders First
- **Bug location:** src/main/java/com/shopsphere/algorithm/OrderPriorityQueue.java
- **How to observe it:** Enqueue a `STANDARD` priority order (level 1) followed by an `EXPRESS` priority order (level 3), then call `pollNextOrder()`.
- **Failure:** The priority queue comparator sorts ascending by priority level (Min-Heap), causing `STANDARD` orders to be polled before `EXPRESS` orders.
- **Expected:** The comparator should sort orders in descending priority order so `EXPRESS` orders dequeue before `PRIORITY` and `STANDARD` orders.

### 4. Warehouse Inventory Search Misses Boundary Products
- **Bug location:** src/main/java/com/shopsphere/util/InventorySearchUtil.java
- **How to observe it:** Perform a binary search for the product at the last index of a sorted inventory list or search within a single-element list.
- **Failure:** The binary search loop condition `low < high` terminates before evaluating the final candidate when `low == high`, returning "Not Found" for existing products.
- **Expected:** The loop condition should be `low <= high` so that boundary elements and single-item lists are inspected and successfully returned.

### 5. Recommendation Graph Traversal Enters Infinite Loop on Cycles
- **Bug location:** src/main/java/com/shopsphere/algorithm/RecommendationGraph.java
- **How to observe it:** Add cyclic product associations (e.g., 101 <-> 102 <-> 103 <-> 101) and request recommendations with a limit greater than reachable nodes.
- **Failure:** Traversal neither checks nor records visited nodes in the `visited` set, continuously re-enqueuing cyclic neighbors until the iteration threshold is exceeded or the thread freezes.
- **Expected:** Traversal should check and update the visited set so already seen nodes are not re-enqueued, terminating cleanly once all reachable nodes are traversed.

### 6. Delivery Route Dijkstra Relaxation Chooses Longer Paths
- **Bug location:** src/main/java/com/shopsphere/algorithm/DeliveryRouteGraph.java
- **How to observe it:** Compute the shortest path between two nodes where a multi-hop path is shorter than a direct road (e.g., direct route is 50 km, while a multi-hop route via waypoint is 25 km).
- **Failure:** Dijkstra relaxation checks `newDist > distances.get(v)` instead of `newDist < distances.get(v)` for discovered nodes, rejecting shorter paths and retaining suboptimal longer routes.
- **Expected:** Relaxation should verify `newDist < distances.get(v)` and update distance and predecessor pointers whenever a shorter route is found.

## Expected Behaviour After Fixing All Bugs

- Price filtering includes products priced exactly at the maximum price boundary (e.g., ₹5,000 items appear in ₹1,000–₹5,000 searches).
- Catalog pagination delivers complete 20-item pages without omitting the 20th product on each page.
- Order dispatch queue prioritizes `EXPRESS` and high-priority deliveries ahead of standard delivery orders.
- Warehouse inventory binary search successfully locates all existing items, including boundary items and single-item inventories, in $O(\log N)$ time.
- Recommendation graph traversal explores cyclic co-purchase graphs without entering infinite loops.
- Delivery routing optimizer finds the shortest path across logistics hubs using Dijkstra's algorithm.
- Standalone test runner (`ChallengeTestRunner`) reports all 6 challenge tests as `passed` with exit code 0.
