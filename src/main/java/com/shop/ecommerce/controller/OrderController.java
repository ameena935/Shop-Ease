package com.shop.ecommerce.controller;

import com.shop.ecommerce.config.ApiException;
import com.shop.ecommerce.dto.Requests;
import com.shop.ecommerce.model.*;
import com.shop.ecommerce.repository.CartItemRepository;
import com.shop.ecommerce.repository.OrderRepository;
import com.shop.ecommerce.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final ProductRepository products;

    public OrderController(OrderRepository orders, CartItemRepository cart, ProductRepository products) {
        this.orders = orders; this.cart = cart; this.products = products;
    }

    /** Checkout: turns the cart into an order and reduces stock. */
    @PostMapping
    @Transactional
    public Order checkout(@RequestAttribute("user") User user, @Valid @RequestBody Requests.Checkout req) {
        List<CartItem> items = cart.findByUserId(user.getId());
        if (items.isEmpty()) throw ApiException.bad("Your cart is empty");

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(user.getName());
        order.setCustomerEmail(user.getEmail());
        order.setAddress(req.address());
        order.setPhone(req.phone());
        order.setPaymentMethod(req.paymentMethod());

        double total = 0;
        for (CartItem ci : items) {
            Product p = ci.getProduct();
            if (ci.getQuantity() > p.getStock())
                throw ApiException.bad("Not enough stock for " + p.getName());
            p.setStock(p.getStock() - ci.getQuantity());
            products.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setImageUrl(p.getImageUrl());
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);
            total += p.getPrice() * ci.getQuantity();
        }
        order.setTotal(total);
        Order saved = orders.save(order);
        cart.deleteByUserId(user.getId());
        return saved;
    }

    @GetMapping
    public List<Order> myOrders(@RequestAttribute("user") User user) {
        return orders.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @GetMapping("/{id}")
    public Order get(@RequestAttribute("user") User user, @PathVariable Long id) {
        return orders.findByIdAndUserId(id, user.getId()).orElseThrow(() -> ApiException.notFound("Order"));
    }

    @PutMapping("/{id}/cancel")
    @Transactional
    public Order cancel(@RequestAttribute("user") User user, @PathVariable Long id) {
        Order o = orders.findByIdAndUserId(id, user.getId()).orElseThrow(() -> ApiException.notFound("Order"));
        if (o.getStatus() != OrderStatus.PLACED) throw ApiException.bad("Only placed orders can be cancelled");
        o.setStatus(OrderStatus.CANCELLED);
        for (OrderItem oi : o.getItems()) {
            products.findById(oi.getProductId()).ifPresent(p -> {
                p.setStock(p.getStock() + oi.getQuantity());
                products.save(p);
            });
        }
        return orders.save(o);
    }
}
