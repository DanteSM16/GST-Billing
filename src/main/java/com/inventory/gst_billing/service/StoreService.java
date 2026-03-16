package com.inventory.gst_billing.service;
import com.inventory.gst_billing.dto.*;
import com.inventory.gst_billing.entity.Store;
import com.inventory.gst_billing.entity.User;
import com.inventory.gst_billing.repository.StoreRepository;
import com.inventory.gst_billing.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {
    private final StoreRepository storeRepo;
    private final UserRepository userRepo;

    public StoreService(StoreRepository storeRepo, UserRepository userRepo) {
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
    }

    public StoreResponse createStore(StoreRequest request) {
        Store store = new Store();
        store.setStoreName(request.getStoreName());
        store.setGstin(request.getGstin());
        store.setStateCode(request.getStateCode().toUpperCase());
        store.setAddress(request.getAddress());
        return mapToResponse(storeRepo.save(store));
    }

    public List<StoreResponse> getAllStores() {
        return storeRepo.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public StoreResponse toggleStoreStatus(Integer id) {
        Store store = storeRepo.findById(id).orElseThrow(() -> new RuntimeException("Store not found"));

        // Toggle the store status
        boolean newStatus = !store.getIsActive();
        store.setIsActive(newStatus);
        storeRepo.save(store);

        // If the store status is made false, remove the emplyees by flipping the status as well.
        if (!newStatus) {
            List<User> storeEmployees = userRepo.findByStoreId(id);
            for (User employee : storeEmployees) {
                employee.setIsActive(false); // Fire them!
                userRepo.save(employee);
            }
        }
        // If we Reactivate the store later, we do NOT automatically rehire the employees.
        // The Owner must manually rehire the staff they actually want to bring back.

        return mapToResponse(store);
    }

    private StoreResponse mapToResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setStoreName(store.getStoreName());
        response.setGstin(store.getGstin());
        response.setStateCode(store.getStateCode());
        response.setAddress(store.getAddress());
        response.setIsActive(store.getIsActive());
        return response;
    }
}