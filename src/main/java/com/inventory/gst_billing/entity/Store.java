package com.inventory.gst_billing.entity;

import jakarta.persistence.*;
import lombok.*;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(nullable = false, length = 15, unique = true)
    private String gstin; // State-specific GST number, gst filings are for each state

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode; // DL ,MH etc

    @Column(length = 255)
    private String address;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
}