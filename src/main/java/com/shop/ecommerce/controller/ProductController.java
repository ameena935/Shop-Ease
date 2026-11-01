package com.shop.ecommerce.controller;

import com.shop.ecommerce.config.ApiException;
import com.shop.ecommerce.model.Category;
import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.repository.CategoryRepository;
import com.shop.ecommerce.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/** Public endpoints: browse, search, filter and sort products. */
@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductController(ProductRepository products, CategoryRepository categories) {
        this.products = products; this.categories = categories;
    }

    @GetMapping("/products")
    public List<Product> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(defaultValue = "newest") String sort) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        List<Product> list = products.search(query, categoryId);
        switch (sort) {
            case "priceAsc" -> list.sort(Comparator.comparingDouble(Product::getPrice));
            case "priceDesc" -> list.sort(Comparator.comparingDouble(Product::getPrice).reversed());
            case "name" -> list.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
            default -> list.sort(Comparator.comparing(Product::getId).reversed());
        }
        return list;
    }

    @GetMapping("/products/{id}")
    public Product get(@PathVariable Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Product"));
    }

    @GetMapping("/categories")
    public List<Category> categories() { return categories.findAll(); }
}
