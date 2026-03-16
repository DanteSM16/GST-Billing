package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Integer> {}