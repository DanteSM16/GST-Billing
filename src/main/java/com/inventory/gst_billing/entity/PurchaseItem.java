package com.inventory.gst_billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

// Template for each line in an invoice that shows the amount bought for each product,
// it maps to one product, the quantity which the amount, the price so we can find total taxable n link to main invoice
// the line it self doesnt contain idefnitifer for each product thats why it is in stock table, the serial noms.

@Entity
@Table(name = "purchase_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // If one invoice has 2 different products brought, it will create 2 rows, one for each product since price/qty
    // and product template have to differ. both of those rows will still point to same invoice so many to one
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    // Again many to one as different peopel can sell us macbook, for each invoice it will create one roe entry for
    // one product/template, 2 invoices contianing macbook create two rows, but will point to same template so Many to one
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductTemplate product;

    @Column(nullable = false)
    private Integer quantity; // Eg 50 MacBooks

    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePrice; // The price negotiated with the supplier
}