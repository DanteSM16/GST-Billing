package com.inventory.gst_billing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sales_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_contact", length = 15)
    private String customerContact;

    @Column(name = "billing_address", length = 255)
    private String billingAddress;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "total_taxable", precision = 15, scale = 2)
    private BigDecimal totalTaxable;

    @Column(name = "cgst_total", precision = 15, scale = 2)
    private BigDecimal cgstTotal;

    @Column(name = "sgst_total", precision = 15, scale = 2)
    private BigDecimal sgstTotal;

    @Column(name = "igst_total", precision = 15, scale = 2)
    private BigDecimal igstTotal;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;

    @Column(name = "customer_gstin", length = 15)
    private String customerGstin; // If null = B2C. If present = B2B.

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
}