package com.shop.ecommerce.repository;

import com.shop.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR CAST(p.id AS string) = :q)")
    List<Product> search(@Param("q") String q, @Param("categoryId") Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);

    long countByCategoryId(Long categoryId);
}
