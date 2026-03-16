package com.inventory.gst_billing.entity;

import com.inventory.gst_billing.enums.StockStatus;
import jakarta.persistence.*;
import lombok.*;

// this maps to in real life the actual products delivered by the supplier, each product evnen if of same type
// is considered unique because of the serial no , ofc maps to a product templae , maps to the invoice so we can
// track the supplier thorugh this and status for telling if it sold or not.
@Entity
@Table(name = "individual_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @Column(name = "serial_no", length = 50)
    private String serialNo; // The physical serial number is the PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductTemplate product; // template, what product it contains

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice; // Where it came from

    @Enumerated(EnumType.STRING) // to store it as string then 0/1 cause default is ordinal type.
    // if use ordinal and latter update data gets corrupted.
    @Column(nullable = false)
    private StockStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
}