package com.inventory.gst_billing.service;


import com.inventory.gst_billing.dto.PaymentOrderResponse;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentOrderResponse createOrder(BigDecimal amountInRupees) throws Exception {

        // 1. Initialize the Razorpay Client with our secure keys
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        // 2. Razorpay ONLY accepts money in the smallest currency unit (Paise).
        // So We multiply by 100.
        BigDecimal amountInPaise = amountInRupees.multiply(new BigDecimal("100"));
        int finalAmount = amountInPaise.intValue();

        // 3. Build the JSON request for Razorpay
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", finalAmount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        // 4. Send it to Razorpay's servers and wait for the Order object back
        Order order = razorpay.orders.create(orderRequest);

        // 5. Extract the ID and send it back
        String orderId = order.get("id");
        return new PaymentOrderResponse(orderId, finalAmount, "INR", keyId);
    }
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            // azorpay's formula: Combine the Order ID and Payment ID with a pipe character
            String payload = orderId + "|" + paymentId;

            // 2. Hash that combined string using yourSecret Key!
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // 3. Convert the byte array back to a Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String generatedSignature = hexString.toString();

            // 4. Compare our generated math against the signature Razorpay sent to the frontend!
            // If they match perfectly, it's not a faked payment since only we have secret key
            return generatedSignature.equals(signature);

        } catch (Exception e) {
            System.err.println("Signature Verification Failed: " + e.getMessage());
            return false;
        }
    }
}