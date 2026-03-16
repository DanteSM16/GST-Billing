package com.inventory.gst_billing.service;

import com.inventory.gst_billing.dto.GstUpdateRequest;
import com.inventory.gst_billing.dto.PriceUpdateRequest;
import com.inventory.gst_billing.dto.ProductTemplateRequest;
import com.inventory.gst_billing.dto.ProductTemplateResponse;
import com.inventory.gst_billing.entity.ProductTemplate;
import com.inventory.gst_billing.repository.ProductTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductTemplateService {

    private final ProductTemplateRepository productRepository;

    public ProductTemplateService(ProductTemplateRepository productRepository) {
        this.productRepository = productRepository;
    }

    // LOGIC FOR CREATE END POINT
    public ProductTemplateResponse createProduct(ProductTemplateRequest request) {
        // Validation by checking if already exists
        if (productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Product with this name already exists!");
        }
        // Map DTO to the entity/java object
        ProductTemplate product = new ProductTemplate();
        product.setName(request.getName());
        product.setHsnCode(request.getHsnCode());
        product.setBasePrice(request.getBasePrice());
        product.setImageUrl(request.getImageUrl());
        product.setGstRate(request.getGstRate());
        // Save to DB
        ProductTemplate savedProduct = productRepository.save(product);
        // Map Entity back to Response DTO
        return mapToResponse(savedProduct);
    }

    // READ Logic
    public List<ProductTemplateResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse) // Convert every DB entity into a Response DTO
                .collect(Collectors.toList());
    }

    // 1. READ by ID
    public ProductTemplateResponse getProductById(Integer id) {
        ProductTemplate product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        return mapToResponse(product);
    }

    // 2. Full Update by using a PUT request
    public ProductTemplateResponse updateProductFull(Integer id, ProductTemplateRequest request) {
        ProductTemplate product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        product.setName(request.getName());
        product.setHsnCode(request.getHsnCode());
        product.setBasePrice(request.getBasePrice());
        product.setGstRate(request.getGstRate());
        product.setImageUrl(request.getImageUrl());

        return mapToResponse(productRepository.save(product));
    }
    // 3. PARTIAL UPDATE (PATCH) for price rise or drop, not used
    public ProductTemplateResponse updatePrice(Integer id, PriceUpdateRequest request) {
        ProductTemplate product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        product.setBasePrice(request.getBasePrice());

        return mapToResponse(productRepository.save(product));
    }

    // 4. PARTIAL UPDATE (PATCH) for gst change, not used
    public ProductTemplateResponse updateGst(Integer id, GstUpdateRequest request) {
        ProductTemplate product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        product.setGstRate(request.getGstRate());

        return mapToResponse(productRepository.save(product));
    }

    // toggling product status for active/inactive
    @Transactional
    public ProductTemplateResponse toggleProductStatus(Integer id) {
        ProductTemplate product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(!product.getIsActive());
        return mapToResponse(productRepository.save(product));
    }

    // Helper Method to avoid writing mapping code every time.
    private ProductTemplateResponse mapToResponse(ProductTemplate product) {
        ProductTemplateResponse response = new ProductTemplateResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setHsnCode(product.getHsnCode());
        response.setBasePrice(product.getBasePrice());
        response.setImageUrl(product.getImageUrl());
        response.setGstRate(product.getGstRate());
        response.setIsActive(product.getIsActive());
        return response;
    }
}