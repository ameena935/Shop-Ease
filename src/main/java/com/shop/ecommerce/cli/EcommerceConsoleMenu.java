package com.shop.ecommerce.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Interactive Console-based E-Commerce Management System.
 * Fully conforms to the Java JDBC Mini Project specifications in the PDF:
 * - Uses a while loop to repeatedly display the menu.
 * - Uses a switch statement to execute selected operations.
 * - Uses Scanner for user input handling.
 * - Implements separate methods for each of the 10 major operations.
 * - Uses JDBC components: Connection, PreparedStatement, and ResultSet.
 * - Uses parameterized SQL queries (?) to prevent SQL injection.
 * - Employs transactions (commit/rollback) to ensure stock and order consistency.
 * - Handles SQLExceptions and input validation gracefully.
 */
@Component
@ConditionalOnProperty(name = "app.console.enabled", havingValue = "true")
public class EcommerceConsoleMenu implements CommandLineRunner {

    private final DataSource dataSource;
    private final Scanner scanner = new Scanner(System.in);
    private boolean running = true;

    // In-memory cart for the console session (productId -> quantity)
    private final Map<Long, Integer> consoleCart = new LinkedHashMap<>();

    public EcommerceConsoleMenu(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        startMenu();
    }

    /**
     * Main loop repeatedly displaying the menu using a while loop and switch statement.
     */
    public void startMenu() {
        System.out.println("\n========================================================");
        System.out.println("  WELCOME TO E-COMMERCE MANAGEMENT SYSTEM (ShopEase)   ");
        System.out.println("========================================================");

        while (running) {
            displayMenu();
            int choice = readIntChoice("Enter your choice (1-10): ");
            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> addProduct();
                case 3 -> updateProduct();
                case 4 -> deleteProduct();
                case 5 -> searchProduct();
                case 6 -> addToCart();
                case 7 -> viewCart();
                case 8 -> placeOrder();
                case 9 -> viewOrderHistory();
                case 10 -> exit();
                default -> System.out.println("❌ Invalid choice! Please select an option from 1 to 10.");
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n--------------------------------------------------------");
        System.out.println("                      MAIN MENU                         ");
        System.out.println("--------------------------------------------------------");
        System.out.println("  1.  View Products");
        System.out.println("  2.  Add Product");
        System.out.println("  3.  Update Product (Price & Stock)");
        System.out.println("  4.  Delete Product");
        System.out.println("  5.  Search Product (by ID or Name)");
        System.out.println("  6.  Add to Cart");
        System.out.println("  7.  View Cart");
        System.out.println("  8.  Place Order (Checkout)");
        System.out.println("  9.  View Order History");
        System.out.println("  10. Exit");
        System.out.println("--------------------------------------------------------");
    }

    // =========================================================================
    // 1. VIEW PRODUCTS
    // =========================================================================
    public void viewProducts() {
        String sql = "SELECT id, name, price, stock FROM products ORDER BY id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n================================================================================");
            System.out.printf("%-6s | %-40s | %-12s | %-10s%n", "ID", "Product Name", "Price (INR)", "Stock");
            System.out.println("--------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                long id = rs.getLong("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");

                String truncatedName = name.length() > 38 ? name.substring(0, 35) + "..." : name;
                System.out.printf("%-6d | %-40s | Rs %-9.2f | %-10d%n", id, truncatedName, price, stock);
            }

            if (!found) {
                System.out.println("No products available in the database.");
            }
            System.out.println("================================================================================");

        } catch (SQLException e) {
            System.err.println("❌ Database error while retrieving products: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. ADD PRODUCT
    // =========================================================================
    public void addProduct() {
        System.out.println("\n--- ADD NEW PRODUCT ---");
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Product name cannot be empty.");
            return;
        }

        System.out.print("Enter Description (optional): ");
        String description = scanner.nextLine().trim();

        double price = readPositiveDouble("Enter Price (INR): ");
        if (price <= 0) {
            System.out.println("❌ Price must be a positive value.");
            return;
        }

        int stock = readPositiveInt("Enter Available Stock Quantity: ");
        if (stock < 0) {
            System.out.println("❌ Stock cannot be negative.");
            return;
        }

        System.out.print("Enter Image URL (or press Enter for default placeholder): ");
        String imageUrl = scanner.nextLine().trim();
        if (imageUrl.isEmpty()) {
            imageUrl = null; // no unrelated stock photo; storefront shows a neutral placeholder
        }

        String sql = "INSERT INTO products (name, description, price, stock, image_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, price);
            ps.setInt(4, stock);
            ps.setString(5, imageUrl);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long generatedId = keys.getLong(1);
                        System.out.printf("✅ Product '%s' added successfully with Product ID: %d!%n", name, generatedId);
                    } else {
                        System.out.printf("✅ Product '%s' added successfully!%n", name);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error while adding product: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. UPDATE PRODUCT
    // =========================================================================
    public void updateProduct() {
        System.out.println("\n--- UPDATE PRODUCT PRICE & STOCK ---");
        long id = readLong("Enter Product ID to update: ");

        // Check if product exists first
        String selectSql = "SELECT id, name, price, stock FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(selectSql)) {

            psSelect.setLong(1, id);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("❌ Product with ID " + id + " not found!");
                    return;
                }

                String currentName = rs.getString("name");
                double currentPrice = rs.getDouble("price");
                int currentStock = rs.getInt("stock");

                System.out.println("Current details for: " + currentName);
                System.out.printf("Current Price: Rs %.2f | Current Stock: %d%n", currentPrice, currentStock);

                double newPrice = readPositiveDouble("Enter New Price (INR, > 0): ");
                int newStock = readPositiveInt("Enter New Stock Quantity (>= 0): ");

                String updateSql = "UPDATE products SET price = ?, stock = ? WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setDouble(1, newPrice);
                    psUpdate.setInt(2, newStock);
                    psUpdate.setLong(3, id);

                    int affected = psUpdate.executeUpdate();
                    if (affected > 0) {
                        System.out.printf("✅ Product ID %d (%s) updated successfully! New Price: Rs %.2f, Stock: %d%n",
                                id, currentName, newPrice, newStock);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error while updating product: " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. DELETE PRODUCT
    // =========================================================================
    public void deleteProduct() {
        System.out.println("\n--- DELETE PRODUCT ---");
        long id = readLong("Enter Product ID to delete: ");

        String selectSql = "SELECT name FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(selectSql)) {

            psSelect.setLong(1, id);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("❌ Product with ID " + id + " not found!");
                    return;
                }
                String name = rs.getString("name");
                System.out.printf("Are you sure you want to delete '%s' (ID: %d)? (yes/no): ", name, id);
                String confirm = scanner.nextLine().trim().toLowerCase();
                if (!confirm.equals("y") && !confirm.equals("yes")) {
                    System.out.println("Operation cancelled.");
                    return;
                }

                // Remove from in-memory console cart if present
                consoleCart.remove(id);

                String deleteSql = "DELETE FROM products WHERE id = ?";
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setLong(1, id);
                    int affected = psDelete.executeUpdate();
                    if (affected > 0) {
                        System.out.printf("✅ Product '%s' (ID: %d) removed from database successfully.%n", name, id);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error while deleting product: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. SEARCH PRODUCT
    // =========================================================================
    public void searchProduct() {
        System.out.println("\n--- SEARCH PRODUCT ---");
        System.out.print("Enter Product ID or Name keyword to search: ");
        String term = scanner.nextLine().trim();

        if (term.isEmpty()) {
            System.out.println("❌ Search term cannot be empty.");
            return;
        }

        // Try numeric ID first, otherwise search by name
        boolean isNumeric = term.matches("\\d+");
        String sql;
        if (isNumeric) {
            sql = "SELECT id, name, price, stock FROM products WHERE id = ? OR LOWER(name) LIKE LOWER(?)";
        } else {
            sql = "SELECT id, name, price, stock FROM products WHERE LOWER(name) LIKE LOWER(?)";
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (isNumeric) {
                ps.setLong(1, Long.parseLong(term));
                ps.setString(2, "%" + term + "%");
            } else {
                ps.setString(1, "%" + term + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n================================================================================");
                System.out.printf("%-6s | %-40s | %-12s | %-10s%n", "ID", "Product Name", "Price (INR)", "Stock");
                System.out.println("--------------------------------------------------------------------------------");

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");
                    String truncatedName = name.length() > 38 ? name.substring(0, 35) + "..." : name;
                    System.out.printf("%-6d | %-40s | Rs %-9.2f | %-10d%n", id, truncatedName, price, stock);
                }

                if (!found) {
                    System.out.println("No matching products found for query: '" + term + "'");
                }
                System.out.println("================================================================================");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error during search: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. ADD TO CART
    // =========================================================================
    public void addToCart() {
        System.out.println("\n--- ADD TO SHOPPING CART ---");
        long productId = readLong("Enter Product ID to add: ");

        String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("❌ Product with ID " + productId + " does not exist!");
                    return;
                }

                String name = rs.getString("name");
                int availableStock = rs.getInt("stock");

                if (availableStock <= 0) {
                    System.out.println("❌ Sorry, '" + name + "' is currently Out of Stock!");
                    return;
                }

                System.out.printf("Selected: '%s' | Available Stock: %d%n", name, availableStock);
                int quantity = readPositiveInt("Enter quantity to add: ");

                if (quantity <= 0) {
                    System.out.println("❌ Quantity must be at least 1.");
                    return;
                }

                int currentInCart = consoleCart.getOrDefault(productId, 0);
                int requestedTotal = currentInCart + quantity;

                // Business Rule: Users cannot purchase products when the available stock is insufficient
                if (requestedTotal > availableStock) {
                    System.out.printf("❌ Insufficient stock! Available: %d, already in cart: %d. Cannot add %d.%n",
                            availableStock, currentInCart, quantity);
                    return;
                }

                consoleCart.put(productId, requestedTotal);
                System.out.printf("✅ Added %d x '%s' to cart! Total in cart: %d%n", quantity, name, requestedTotal);
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error while adding to cart: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. VIEW CART
    // =========================================================================
    public void viewCart() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                              YOUR SHOPPING CART                                ");
        System.out.println("--------------------------------------------------------------------------------");

        if (consoleCart.isEmpty()) {
            System.out.println("Your shopping cart is currently empty.");
            System.out.println("--------------------------------------------------------------------------------");
            return;
        }

        System.out.printf("%-6s | %-35s | %-12s | %-8s | %-12s%n", "ID", "Product Name", "Unit Price", "Quantity", "Subtotal");
        System.out.println("--------------------------------------------------------------------------------");

        double grandTotal = 0;
        String sql = "SELECT id, name, price, stock FROM products WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Map.Entry<Long, Integer> entry : consoleCart.entrySet()) {
                long productId = entry.getKey();
                int qty = entry.getValue();

                ps.setLong(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        double subtotal = price * qty;
                        grandTotal += subtotal;

                        String truncatedName = name.length() > 33 ? name.substring(0, 30) + "..." : name;
                        System.out.printf("%-6d | %-35s | Rs %-9.2f | %-8d | Rs %-9.2f%n",
                                productId, truncatedName, price, qty, subtotal);
                    }
                }
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("%59s: Rs %.2f%n", "CART TOTAL AMOUNT", grandTotal);
            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("❌ Database error reading cart items: " + e.getMessage());
        }
    }

    // =========================================================================
    // 8. PLACE ORDER (ATOMIC TRANSACTION WITH STOCK DEDUCTION)
    // =========================================================================
    public void placeOrder() {
        System.out.println("\n--- PLACE ORDER (CHECKOUT) ---");
        if (consoleCart.isEmpty()) {
            System.out.println("❌ Your shopping cart is empty. Add items before placing an order.");
            return;
        }

        viewCart();

        System.out.print("\nDo you confirm this order? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Order placement cancelled.");
            return;
        }

        System.out.print("Enter Customer Name: ");
        String customerName = scanner.nextLine().trim();
        if (customerName.isEmpty()) customerName = "Walk-in Customer";

        System.out.print("Enter Shipping Address: ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) address = "Counter Pickup";

        System.out.print("Enter Contact Phone (10 digits): ");
        String phone = scanner.nextLine().trim();

        // Transactional execution ensuring atomicity (ACID compliance)
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step 1: Verify all products and available stock
            double totalAmount = 0.0;
            List<OrderItemRecord> itemsToOrder = new ArrayList<>();

            String checkSql = "SELECT id, name, price, stock, image_url FROM products WHERE id = ? FOR UPDATE";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                for (Map.Entry<Long, Integer> entry : consoleCart.entrySet()) {
                    long productId = entry.getKey();
                    int requestedQty = entry.getValue();

                    psCheck.setLong(1, productId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product ID " + productId + " no longer exists.");
                        }

                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        int availableStock = rs.getInt("stock");
                        String imageUrl = rs.getString("image_url");

                        // Business Rule: Users cannot purchase products when the available stock is insufficient
                        if (requestedQty > availableStock) {
                            throw new SQLException("Insufficient stock for '" + name + "'. Available: " +
                                    availableStock + ", Requested: " + requestedQty);
                        }

                        double subtotal = price * requestedQty;
                        totalAmount += subtotal;
                        itemsToOrder.add(new OrderItemRecord(productId, name, price, requestedQty, imageUrl));
                    }
                }
            }

            // Step 2: Insert record into `orders` table
            long orderId = -1;
            String orderSql = "INSERT INTO orders (customer_name, customer_email, address, phone, payment_method, total_amount, status, order_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setString(1, customerName);
                psOrder.setString(2, "customer@store.com");
                psOrder.setString(3, address);
                psOrder.setString(4, phone);
                psOrder.setString(5, "CASH_ON_DELIVERY");
                psOrder.setDouble(6, totalAmount);
                psOrder.setString(7, "PLACED");
                psOrder.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

                psOrder.executeUpdate();
                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getLong(1);
                    }
                }
            }

            if (orderId == -1) {
                throw new SQLException("Failed to generate order ID.");
            }

            // Step 3: Insert each item into `order_items` table and update product stock
            String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, image_url, purchase_price, quantity) VALUES (?, ?, ?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";

            try (PreparedStatement psItem = conn.prepareStatement(itemSql);
                 PreparedStatement psStock = conn.prepareStatement(updateStockSql)) {

                for (OrderItemRecord item : itemsToOrder) {
                    // Save item
                    psItem.setLong(1, orderId);
                    psItem.setLong(2, item.productId());
                    psItem.setString(3, item.name());
                    psItem.setString(4, item.imageUrl());
                    psItem.setDouble(5, item.price());
                    psItem.setInt(6, item.quantity());
                    psItem.executeUpdate();

                    // Update stock
                    psStock.setInt(1, item.quantity());
                    psStock.setLong(2, item.productId());
                    psStock.executeUpdate();
                }
            }

            // Commit transaction
            conn.commit();
            consoleCart.clear();

            System.out.println("\n🎉 ========================================================");
            System.out.println("                 ORDER PLACED SUCCESSFULLY!                 ");
            System.out.println("============================================================");
            System.out.println("  Order ID      : #" + orderId);
            System.out.println("  Customer Name : " + customerName);
            System.out.println("  Delivery To   : " + address);
            System.out.printf("  Total Bill    : Rs %.2f%n", totalAmount);
            System.out.println("  Payment Method: Cash On Delivery");
            System.out.println("  Status        : PLACED (Stock updated in database)");
            System.out.println("============================================================\n");

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("⚠️  Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Order placement failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // ignore
                }
            }
        }
    }

    // =========================================================================
    // 9. VIEW ORDER HISTORY
    // =========================================================================
    public void viewOrderHistory() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                              ORDER HISTORY                                     ");
        System.out.println("--------------------------------------------------------------------------------");

        String orderSql = "SELECT id, customer_name, total_amount, status, order_date FROM orders ORDER BY id DESC";
        String itemsSql = "SELECT product_name, purchase_price, quantity FROM order_items WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement psOrder = conn.prepareStatement(orderSql);
             PreparedStatement psItems = conn.prepareStatement(itemsSql);
             ResultSet rsOrder = psOrder.executeQuery()) {

            boolean hasOrders = false;
            while (rsOrder.next()) {
                hasOrders = true;
                long orderId = rsOrder.getLong("id");
                String customer = rsOrder.getString("customer_name");
                double total = rsOrder.getDouble("total_amount");
                String status = rsOrder.getString("status");
                Timestamp date = rsOrder.getTimestamp("order_date");

                System.out.printf("Order #%-4d | Date: %-19s | Customer: %-15s | Total: Rs %-9.2f | Status: %s%n",
                        orderId, date != null ? date.toString().substring(0, 19) : "N/A", customer, total, status);

                psItems.setLong(1, orderId);
                try (ResultSet rsItems = psItems.executeQuery()) {
                    while (rsItems.next()) {
                        System.out.printf("   └─ %s (Qty: %d) @ Rs %.2f each%n",
                                rsItems.getString("product_name"),
                                rsItems.getInt("quantity"),
                                rsItems.getDouble("purchase_price"));
                    }
                }
                System.out.println("--------------------------------------------------------------------------------");
            }

            if (!hasOrders) {
                System.out.println("No orders have been placed yet.");
                System.out.println("--------------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("❌ Database error retrieving order history: " + e.getMessage());
        }
    }

    // =========================================================================
    // 10. EXIT
    // =========================================================================
    public void exit() {
        running = false;
        System.out.println("\n========================================================");
        System.out.println(" Thank you for using ShopEase E-Commerce System! Goodbye.");
        System.out.println("========================================================\n");
    }

    // Helper input methods
    private int readIntChoice(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Long.parseLong(line);
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid numerical ID.");
            }
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                double val = Double.parseDouble(line);
                if (val > 0) return val;
                System.out.println("❌ Value must be strictly positive (> 0).");
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid decimal number.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                int val = Integer.parseInt(line);
                if (val >= 0) return val;
                System.out.println("❌ Quantity cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid integer.");
            }
        }
    }

    private record OrderItemRecord(long productId, String name, double price, int quantity, String imageUrl) {}
}
