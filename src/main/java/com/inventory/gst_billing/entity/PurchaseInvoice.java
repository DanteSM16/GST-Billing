package com.inventory.gst_billing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// this table maps to the acutal invoice snet in by the supplier, it containes their details , net payable and the gst
// break up, the list of items bought and thier quanities and details come in the seconaary invoice with each lien repesenting oen item

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "purchase_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "supplier_gstin", nullable = false, length = 15)
    private String supplierGstin;

    @Column(name = "billing_address", length = 255)
    private String billingAddress;

    @Column(name = "contact_number", length = 15)
    private String contactNumber;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode; // Determines IGST vs CGST/SGST

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

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
}