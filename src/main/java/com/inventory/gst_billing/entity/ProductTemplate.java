package com.inventory.gst_billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product_templates", indexes = {
        @Index(name = "idx_product_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "hsn_code", nullable = false, length = 8)
    private String hsnCode; // Mandatory for Indian GST

    @Column(nullable = false, length = 100, unique = true)
    private String name; // e.g., "Name of the product, eg MacBook Pro 14 M3"

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice; // Big decimal for accurate math and calculations instead of Double

    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate; // eg 18.00, add in terms of percentage

    // added later for adding images in forntend
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    @CreatedBy
    @Column(updatable = false)
    private String createdBy;


    @LastModifiedBy
    private String lastModifiedBy;
}