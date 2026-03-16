package com.inventory.gst_billing.repository;


import com.inventory.gst_billing.entity.ProductTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Integer> {
    // Spring JPA  will write SQL to check if a product name already exists to avoid duplication. Query derivation used.
    boolean existsByName(String name);
}