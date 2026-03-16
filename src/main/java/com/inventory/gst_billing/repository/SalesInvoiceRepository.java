package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.dto.B2bB2cSummaryDto;
import com.inventory.gst_billing.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Integer> {
    @Query(
            "SELECT new com.inventory.gst_billing.dto.B2bB2cSummaryDto(" +
                    "CASE WHEN s.customerGstin IS NULL THEN 'B2C' ELSE 'B2B' END, SUM(s.totalTaxable), SUM(s.cgstTotal + s.sgstTotal + s.igstTotal)) " +
                    "FROM SalesInvoice s WHERE s.store.stateCode = :stateCode AND s.createdAt BETWEEN :start AND :end " +
                    "GROUP BY CASE WHEN s.customerGstin IS NULL THEN 'B2C' ELSE 'B2B' END")
    List<B2bB2cSummaryDto> getB2bB2cSummary(String stateCode, java.time.LocalDateTime start, java.time.LocalDateTime end);


}