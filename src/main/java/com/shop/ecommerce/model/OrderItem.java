package com.shop.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private String productName;

    @Column(length = 1000)
    private String imageUrl;

    @Column(name = "purchase_price", nullable = false)
    private double price;

    @Column(nullable = false)
    private int quantity;

    // Aliases to conform directly with PDF specifications
    public double getPurchasePrice() {
        return this.price;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.price = purchasePrice;
    }
}
