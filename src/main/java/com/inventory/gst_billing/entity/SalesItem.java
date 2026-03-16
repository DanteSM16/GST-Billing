package com.inventory.gst_billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sales_items", indexes = {
        @Index(name = "idx_salesitem_invoice", columnList = "sales_invoice_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_serial_no", nullable = false, unique = true)
    private Stock stock; // 1 Sales line explicitly links to 1 unique Serial Number

    @Column(name = "selling_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal sellingPrice; // The exact price sold to customer
}