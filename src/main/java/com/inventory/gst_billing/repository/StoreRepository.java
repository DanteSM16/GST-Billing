package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Integer> {
    // Derived query to find all stores inside a specific state for GST filing
    List<Store> findByStateCode(String stateCode);
}