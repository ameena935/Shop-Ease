package com.shop.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(length = 1000)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category;

    // Alias methods to directly reflect PDF schema nomenclature
    public int getStockQuantity() {
        return this.stock;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stock = stockQuantity;
    }
}
