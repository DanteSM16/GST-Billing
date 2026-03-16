package com.inventory.gst_billing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inventory.gst_billing.enums.GstComponent;
import com.inventory.gst_billing.enums.GstType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "gst_ledger",
        indexes = {@Index(name = "idx_ledger_store_date", columnList = "store_id, created_at")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GstLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GstComponent component; // CGST, SGST, IGST

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GstType type; // INPUT or OUTPUT

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", nullable = false)
    private Integer transactionId; // ID of the Purchase or Sale Invoice, soft foreign key not direct link

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // "PURCHASE" or "SALE", neede since we soft link no direct to purchase/sales table

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