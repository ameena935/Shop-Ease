package com.shop.ecommerce.controller;

import com.shop.ecommerce.config.ApiException;
import com.shop.ecommerce.dto.Requests;
import com.shop.ecommerce.model.CartItem;
import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.repository.CartItemRepository;
import com.shop.ecommerce.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartItemRepository cart;
    private final ProductRepository products;

    public CartController(CartItemRepository cart, ProductRepository products) {
        this.cart = cart; this.products = products;
    }

    @GetMapping
    public List<CartItem> view(@RequestAttribute("user") User user) {
        return cart.findByUserId(user.getId());
    }

    @PostMapping
    public CartItem add(@RequestAttribute("user") User user, @Valid @RequestBody Requests.CartAdd req) {
        Product p = products.findById(req.productId()).orElseThrow(() -> ApiException.notFound("Product"));
        CartItem item = cart.findByUserIdAndProductId(user.getId(), p.getId()).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUser(user); c.setProduct(p); c.setQuantity(0);
            return c;
        });
        int newQty = item.getQuantity() + req.quantity();
        if (newQty > p.getStock()) throw ApiException.bad("Only " + p.getStock() + " in stock");
        item.setQuantity(newQty);
        return cart.save(item);
    }

    @PutMapping("/{itemId}")
    public CartItem update(@RequestAttribute("user") User user, @PathVariable Long itemId,
                           @Valid @RequestBody Requests.CartUpdate req) {
        CartItem item = cart.findByIdAndUserId(itemId, user.getId()).orElseThrow(() -> ApiException.notFound("Cart item"));
        if (req.quantity() > item.getProduct().getStock())
            throw ApiException.bad("Only " + item.getProduct().getStock() + " in stock");
        item.setQuantity(req.quantity());
        return cart.save(item);
    }

    @DeleteMapping("/{itemId}")
    public Map<String, String> remove(@RequestAttribute("user") User user, @PathVariable Long itemId) {
        CartItem item = cart.findByIdAndUserId(itemId, user.getId()).orElseThrow(() -> ApiException.notFound("Cart item"));
        cart.delete(item);
        return Map.of("message", "Removed");
    }

    @DeleteMapping
    public Map<String, String> clear(@RequestAttribute("user") User user) {
        cart.deleteByUserId(user.getId());
        return Map.of("message", "Cart cleared");
    }
}
