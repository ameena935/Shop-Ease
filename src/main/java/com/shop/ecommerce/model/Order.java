package com.shop.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "ShopOrder")
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String customerName;
    private String customerEmail;
    private String address;
    private String phone;
    private String paymentMethod;

    @Column(name = "total_amount", nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "order_date")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Aliases to conform directly with PDF specifications
    public LocalDateTime getOrderDate() {
        return this.createdAt;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.createdAt = orderDate;
    }

    public double getTotalAmount() {
        return this.total;
    }

    public void setTotalAmount(double totalAmount) {
        this.total = totalAmount;
    }
}
