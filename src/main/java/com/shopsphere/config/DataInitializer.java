package com.shopsphere.config;

import com.shopsphere.algorithm.OrderPriorityQueue;
import com.shopsphere.algorithm.RecommendationGraph;
import com.shopsphere.entity.*;
import com.shopsphere.repository.*;
import com.shopsphere.service.RouteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OrderRepository orderRepository;
    private final RoadEdgeRepository roadEdgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecommendationGraph recommendationGraph;
    private final OrderPriorityQueue orderPriorityQueue;
    private final RouteService routeService;

    public DataInitializer(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CustomerRepository customerRepository,
                           WarehouseRepository warehouseRepository,
                           InventoryItemRepository inventoryItemRepository,
                           OrderRepository orderRepository,
                           RoadEdgeRepository roadEdgeRepository,
                           PasswordEncoder passwordEncoder,
                           RecommendationGraph recommendationGraph,
                           OrderPriorityQueue orderPriorityQueue,
                           RouteService routeService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.orderRepository = orderRepository;
        this.roadEdgeRepository = roadEdgeRepository;
        this.passwordEncoder = passwordEncoder;
        this.recommendationGraph = recommendationGraph;
        this.orderPriorityQueue = orderPriorityQueue;
        this.routeService = routeService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        System.out.println("Initializing ShopSphere Pro Seed Data...");

        // 1. Seed 50 Categories
        List<Category> categories = new ArrayList<>();
        String[] categoryNames = {
                "Smartphones", "Laptops", "Tablets", "Smartwatches", "Headphones",
                "Monitors", "Keyboards", "Mice", "Printers", "Cameras",
                "Televisions", "Speakers", "Gaming Consoles", "Smart Home", "Routers",
                "Hard Drives", "Power Banks", "Cables & Adapters", "Men Fashion", "Women Fashion",
                "Footwear", "Watches", "Luggage & Bags", "Eyewear", "Jewelry",
                "Kitchen Appliances", "Cookware", "Home Decor", "Furniture", "Bedding",
                "Bath & Sanitary", "Lighting", "Gardening Tools", "Cleaning Supplies", "Pet Supplies",
                "Fitness Equipment", "Outdoor Recreation", "Cycling", "Team Sports", "Camping & Hiking",
                "Books & Novels", "Board Games", "Stationery", "Art Supplies", "Baby Care",
                "Skincare", "Haircare", "Fragrances", "Automotive Tools", "Safety Equipment"
        };

        for (int i = 0; i < categoryNames.length; i++) {
            Category cat = new Category(null, categoryNames[i], "CAT-" + (i + 1), "Category for " + categoryNames[i]);
            categories.add(cat);
        }
        categories = categoryRepository.saveAll(categories);

        // 2. Seed 8 Warehouses
        List<Warehouse> warehouses = new ArrayList<>();
        String[][] whData = {
                {"WH-DELHI", "ShopSphere North Hub", "New Delhi", "NODE_DELHI", "100000"},
                {"WH-MUMBAI", "ShopSphere West Hub", "Mumbai", "NODE_MUMBAI", "120000"},
                {"WH-BANGALORE", "ShopSphere South Hub", "Bengaluru", "NODE_BANGALORE", "150000"},
                {"WH-HYDERABAD", "ShopSphere Deccan Hub", "Hyderabad", "NODE_HYDERABAD", "80000"},
                {"WH-KOLKATA", "ShopSphere East Hub", "Kolkata", "NODE_KOLKATA", "90000"},
                {"WH-CHENNAI", "ShopSphere Coastal Hub", "Chennai", "NODE_CHENNAI", "75000"},
                {"WH-PUNE", "ShopSphere Express Hub", "Pune", "NODE_PUNE", "60000"},
                {"WH-AHMEDABAD", "ShopSphere Gujarat Hub", "Ahmedabad", "NODE_AHMEDABAD", "70000"}
        };

        for (String[] data : whData) {
            Warehouse wh = new Warehouse(null, data[0], data[1], data[2], data[3], Integer.parseInt(data[4]));
            warehouses.add(wh);
        }
        warehouses = warehouseRepository.saveAll(warehouses);

        // 3. Seed 500 Products (including boundary products at ₹5000.0 for Bug 1)
        List<Product> products = new ArrayList<>();
        String[] brands = {"Apple", "Samsung", "Sony", "Dell", "HP", "Nike", "Adidas", "Puma", "Philips", "LG", "Bose", "Asus", "Lenovo", "Logitech"};

        for (int i = 1; i <= 500; i++) {
            Category cat = categories.get((i - 1) % categories.size());
            String brand = brands[(i - 1) % brands.length];
            double price;

            // Bug 1 target products: specific products priced exactly 5000.00
            if (i == 50 || i == 100 || i == 150) {
                price = 5000.00;
            } else if (i == 200) {
                price = 1000.00;
            } else {
                price = Math.round((299.0 + (i * 173.0) % 95000) * 100.0) / 100.0;
            }

            double rating = Math.round((3.0 + (i % 21) * 0.1) * 10.0) / 10.0;
            int stock = 20 + (i * 17) % 300;

            Product p = new Product(
                    null,
                    brand + " " + cat.getName() + " Model Pro " + i,
                    "High performance " + cat.getName() + " manufactured by " + brand + ". Reliable and durable.",
                    cat,
                    brand,
                    price,
                    rating,
                    stock,
                    "SKU-" + String.format("%04d", i)
            );
            products.add(p);
        }
        products = productRepository.saveAll(products);

        // 4. Seed 300 Customers with standard roles & test accounts
        List<Customer> customers = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("Password@123");

        // Fixed system accounts
        customers.add(new Customer(null, "System Administrator", "admin@shopsphere.com", passwordEncoder.encode("Admin@123"), Role.ADMIN, "7th Avenue Headquarters", "New Delhi", "NODE_DELHI"));
        customers.add(new Customer(null, "Warehouse Manager", "manager@shopsphere.com", passwordEncoder.encode("Manager@123"), Role.WAREHOUSE_MANAGER, "Warehouse Complex Gate 4", "Mumbai", "NODE_MUMBAI"));
        customers.add(new Customer(null, "Delivery Partner", "delivery@shopsphere.com", passwordEncoder.encode("Delivery@123"), Role.DELIVERY_PARTNER, "Logistics Center East", "Bengaluru", "NODE_BANGALORE"));
        customers.add(new Customer(null, "Primary Customer", "customer@shopsphere.com", passwordEncoder.encode("Customer@123"), Role.CUSTOMER, "42 Tech Residency", "Pune", "NODE_PUNE"));

        String[] cities = {"New Delhi", "Mumbai", "Bengaluru", "Hyderabad", "Kolkata", "Chennai", "Pune", "Ahmedabad", "Jaipur", "Lucknow", "Chandigarh", "Indore"};
        String[] nodes = {"NODE_DELHI", "NODE_MUMBAI", "NODE_BANGALORE", "NODE_HYDERABAD", "NODE_KOLKATA", "NODE_CHENNAI", "NODE_PUNE", "NODE_AHMEDABAD", "NODE_JAIPUR", "NODE_LUCKNOW"};

        for (int i = 5; i <= 300; i++) {
            String city = cities[(i - 1) % cities.length];
            String node = nodes[(i - 1) % nodes.length];
            Customer c = new Customer(
                    null,
                    "Customer " + i,
                    "customer" + i + "@example.com",
                    encodedPassword,
                    Role.CUSTOMER,
                    "Flat " + (100 + i) + ", Sector " + (i % 25) + ", " + city,
                    city,
                    node
            );
            customers.add(c);
        }
        customers = customerRepository.saveAll(customers);

        // 5. Seed 150 Inventory Locations
        List<InventoryItem> inventoryItems = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            Product p = products.get(i);
            Warehouse wh = warehouses.get(i % warehouses.size());
            String aisle = "Aisle-" + ((i / 10) + 1);
            String bin = "Bin-" + (char) ('A' + (i % 26)) + "-" + (i % 50);
            int qty = (i % 12 == 0) ? 5 : (30 + (i * 7) % 200); // Some items trigger low stock alert (<= 10)
            int minThreshold = 10;

            InventoryItem item = new InventoryItem(null, p, wh, aisle, bin, qty, minThreshold);
            inventoryItems.add(item);
        }
        inventoryItemRepository.saveAll(inventoryItems);

        // 6. Seed 700 Orders
        List<Order> orders = new ArrayList<>();
        Random random = new Random(42);
        OrderStatus[] statuses = OrderStatus.values();
        OrderPriority[] priorities = OrderPriority.values();

        for (int i = 1; i <= 700; i++) {
            Customer cust = customers.get(random.nextInt(customers.size()));
            OrderStatus status = statuses[i % statuses.length];
            OrderPriority priority = priorities[i % priorities.length];
            Warehouse wh = warehouses.get(random.nextInt(warehouses.size()));

            Order order = new Order(
                    null,
                    "ORD-" + String.format("%05d", i),
                    cust,
                    LocalDateTime.now().minusDays(i % 60).minusHours(i % 24),
                    status,
                    priority,
                    0.0,
                    cust.getAddress(),
                    cust.getDeliveryNodeId(),
                    wh
            );

            int itemCount = 1 + (i % 4);
            double total = 0.0;
            for (int j = 0; j < itemCount; j++) {
                Product prod = products.get((i * 3 + j) % products.size());
                int qty = 1 + (j % 3);
                double sub = prod.getPrice() * qty;
                OrderItem item = new OrderItem(null, order, prod, qty, prod.getPrice(), sub);
                order.addItem(item);
                total += sub;
            }
            order.setTotalAmount(Math.round(total * 100.0) / 100.0);
            orders.add(order);

            // Populate initial queue with PENDING orders
            if (status == OrderStatus.PENDING) {
                orderPriorityQueue.enqueue(order);
            }
        }
        orderRepository.saveAll(orders);

        // 7. Seed Road Network for Delivery Routing & Shortest Path (Bug 6)
        List<RoadEdge> roadEdges = new ArrayList<>();
        // Road edges connecting nodes
        // Special test scenario for Bug 6:
        // Direct route: NODE_DELHI -> NODE_JAIPUR is 280 km
        // Via Hub: NODE_DELHI -> NODE_GURUGRAM (30 km), NODE_GURUGRAM -> NODE_JAIPUR (210 km) = 240 km (Shorter!)
        roadEdges.add(new RoadEdge(null, "NODE_DELHI", "NODE_JAIPUR", 280.0));
        roadEdges.add(new RoadEdge(null, "NODE_DELHI", "NODE_GURUGRAM", 30.0));
        roadEdges.add(new RoadEdge(null, "NODE_GURUGRAM", "NODE_JAIPUR", 210.0));

        roadEdges.add(new RoadEdge(null, "NODE_DELHI", "NODE_LUCKNOW", 500.0));
        roadEdges.add(new RoadEdge(null, "NODE_DELHI", "NODE_AHMEDABAD", 900.0));
        roadEdges.add(new RoadEdge(null, "NODE_AHMEDABAD", "NODE_MUMBAI", 520.0));
        roadEdges.add(new RoadEdge(null, "NODE_MUMBAI", "NODE_PUNE", 150.0));
        roadEdges.add(new RoadEdge(null, "NODE_PUNE", "NODE_BANGALORE", 840.0));
        roadEdges.add(new RoadEdge(null, "NODE_BANGALORE", "NODE_HYDERABAD", 570.0));
        roadEdges.add(new RoadEdge(null, "NODE_BANGALORE", "NODE_CHENNAI", 350.0));
        roadEdges.add(new RoadEdge(null, "NODE_HYDERABAD", "NODE_KOLKATA", 1500.0));
        roadEdges.add(new RoadEdge(null, "NODE_LUCKNOW", "NODE_KOLKATA", 980.0));
        roadEdgeRepository.saveAll(roadEdges);
        routeService.syncGraphFromRepository();

        // 8. Seed Recommendation Graph with cycles (to test Bug 5)
        // Products 1, 2, 3 form a cyclic co-purchase triangle
        recommendationGraph.addEdge(products.get(0).getId(), products.get(1).getId());
        recommendationGraph.addEdge(products.get(1).getId(), products.get(2).getId());
        recommendationGraph.addEdge(products.get(2).getId(), products.get(0).getId());

        // Products 4, 5, 6 form another cyclic cluster
        recommendationGraph.addEdge(products.get(3).getId(), products.get(4).getId());
        recommendationGraph.addEdge(products.get(4).getId(), products.get(5).getId());
        recommendationGraph.addEdge(products.get(5).getId(), products.get(3).getId());

        // Connect a few co-purchases from orders
        for (int i = 0; i < 20; i++) {
            recommendationGraph.addEdge(products.get(i).getId(), products.get(i + 5).getId());
        }

        // Isolated cyclic cluster (products 490, 491, 492) specifically triggering Bug 5 on web UI
        recommendationGraph.addEdge(products.get(489).getId(), products.get(490).getId());
        recommendationGraph.addEdge(products.get(490).getId(), products.get(491).getId());
        recommendationGraph.addEdge(products.get(491).getId(), products.get(489).getId());

        System.out.println("ShopSphere Pro Seed Data Initialized Successfully.");
    }
}
