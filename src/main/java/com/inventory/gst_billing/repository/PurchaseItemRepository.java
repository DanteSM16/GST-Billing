package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Integer> {
    List<PurchaseItem> findByPurchaseInvoiceId(Integer invoiceId);
}