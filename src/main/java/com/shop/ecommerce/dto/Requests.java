package com.shop.ecommerce.dto;

import jakarta.validation.constraints.*;

public class Requests {
    public record Register(@NotBlank String name, @Email @NotBlank String email,
                           @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {}

    public record Login(@NotBlank String email, @NotBlank String password) {}

    public record CartAdd(@NotNull Long productId, @Min(1) int quantity) {}

    public record CartUpdate(@Min(1) int quantity) {}

    public record Checkout(@NotBlank String address,
                           @NotBlank @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits") String phone,
                           @NotBlank String paymentMethod) {}

    public record ProductForm(@NotBlank String name, String description,
                              @PositiveOrZero double price, @PositiveOrZero int stock,
                              String imageUrl, Long categoryId) {}

    public record CategoryForm(@NotBlank String name) {}

    public record StatusUpdate(@NotBlank String status) {}
}
