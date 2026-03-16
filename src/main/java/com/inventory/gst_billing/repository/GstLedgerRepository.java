package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.entity.GstLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GstLedgerRepository extends JpaRepository<GstLedger, Integer> {
    List<GstLedger> findByStoreIdIn(List<Integer> storeIds);
    // Fetch ledger entries for specific stores WITHIN a specific date range!
    List<GstLedger> findByStoreIdInAndCreatedAtBetween(List<Integer> storeIds, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
