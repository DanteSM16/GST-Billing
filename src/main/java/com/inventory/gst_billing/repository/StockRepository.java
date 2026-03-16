package com.inventory.gst_billing.repository;

import com.inventory.gst_billing.entity.Stock;
import com.inventory.gst_billing.enums.StockStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {
    //List<Stock> findByProductIdAndStatus(Integer productId, StockStatus status);
    List<Stock> findByProductIdAndStatusAndStoreId(Integer productId, StockStatus status, Integer storeId);
}