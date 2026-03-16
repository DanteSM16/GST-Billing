package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.dto.HsnSummaryDto;
import com.inventory.gst_billing.entity.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesItemRepository extends JpaRepository<SalesItem, Integer> {
    List<SalesItem> findBySalesInvoiceId(Integer invoiceId);


    @Query(
            "SELECT new com.inventory.gst_billing.dto.HsnSummaryDto(" + // <-- Added underscore here
                    "s.stock.product.hsnCode, SUM(s.sellingPrice), SUM(s.sellingPrice * s.stock.product.gstRate / 100)) " +
                    "FROM SalesItem s WHERE s.salesInvoice.store.stateCode = :stateCode AND s.salesInvoice.createdAt BETWEEN :start AND :end " +
                    "GROUP BY s.stock.product.hsnCode")
    List<HsnSummaryDto> getHsnSummary(String stateCode, java.time.LocalDateTime start, java.time.LocalDateTime end);
}