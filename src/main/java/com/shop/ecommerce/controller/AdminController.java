package com.shop.ecommerce.controller;

import com.shop.ecommerce.config.ApiException;
import com.shop.ecommerce.dto.Requests;
import com.shop.ecommerce.model.*;
import com.shop.ecommerce.repository.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin-only endpoints (role checked in AuthInterceptor). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final OrderRepository orders;
    private final UserRepository users;
    private final CartItemRepository cart;

    public AdminController(ProductRepository p, CategoryRepository c, OrderRepository o, UserRepository u, CartItemRepository ci) {
        products = p; categories = c; orders = o; users = u; cart = ci;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Order> all = orders.findAll();
        double revenue = all.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED).mapToDouble(Order::getTotal).sum();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("products", products.count());
        m.put("orders", all.size());
        m.put("users", users.count());
        m.put("revenue", revenue);
        m.put("pending", all.stream().filter(o -> o.getStatus() == OrderStatus.PLACED).count());
        m.put("lowStock", products.findAll().stream().filter(p -> p.getStock() < 10).count());
        return m;
    }

    // ---------- Products ----------
    @PostMapping("/products")
    public Product create(@Valid @RequestBody Requests.ProductForm f) {
        return products.save(apply(new Product(), f));
    }

    @PutMapping("/products/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody Requests.ProductForm f) {
        Product p = products.findById(id).orElseThrow(() -> ApiException.notFound("Product"));
        return products.save(apply(p, f));
    }

    @DeleteMapping("/products/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        if (!products.existsById(id)) throw ApiException.notFound("Product");
        cart.deleteByProductId(id);
        products.deleteById(id);
        return Map.of("message", "Product deleted");
    }

    private Product apply(Product p, Requests.ProductForm f) {
        p.setName(f.name());
        p.setDescription(f.description());
        p.setPrice(f.price());
        p.setStock(f.stock());
        p.setImageUrl(f.imageUrl());
        p.setCategory(f.categoryId() == null ? null :
                categories.findById(f.categoryId()).orElseThrow(() -> ApiException.notFound("Category")));
        return p;
    }

    // ---------- Categories ----------
    @PostMapping("/categories")
    public Category addCategory(@Valid @RequestBody Requests.CategoryForm f) {
        if (categories.existsByNameIgnoreCase(f.name().trim())) throw ApiException.bad("Category already exists");
        return categories.save(new Category(f.name().trim()));
    }

    @DeleteMapping("/categories/{id}")
    public Map<String, String> deleteCategory(@PathVariable Long id) {
        if (products.countByCategoryId(id) > 0) throw ApiException.bad("Category has products — move or delete them first");
        categories.deleteById(id);
        return Map.of("message", "Category deleted");
    }

    // ---------- Orders & Users ----------
    @GetMapping("/orders")
    public List<Order> allOrders() { return orders.findAllByOrderByCreatedAtDesc(); }

    @PutMapping("/orders/{id}/status")
    public Order setStatus(@PathVariable Long id, @Valid @RequestBody Requests.StatusUpdate req) {
        Order o = orders.findById(id).orElseThrow(() -> ApiException.notFound("Order"));
        try {
            o.setStatus(OrderStatus.valueOf(req.status().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw ApiException.bad("Invalid status");
        }
        return orders.save(o);
    }

    @GetMapping("/users")
    public List<User> allUsers() { return users.findAll(); }
}
