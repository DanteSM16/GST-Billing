package com.inventory.gst_billing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GstCalculationResponse {

    private String stateCode;
    // Total Input Tax Credit (Money the government owes you)
    private BigDecimal totalInputIgst;
    private BigDecimal totalInputCgst;
    private BigDecimal totalInputSgst;

    // Total Output Liability (Money you collected from customers)
    private BigDecimal totalOutputIgst;
    private BigDecimal totalOutputCgst;
    private BigDecimal totalOutputSgst;

    // Final Net Payable (The actual cash you have to wire to the government this month)
    private BigDecimal netPayableIgst;
    private BigDecimal netPayableCgst;
    private BigDecimal netPayableSgst;

    // NEW: Leftover Credit to carry forward to next month!
    private BigDecimal leftoverInputIgst;
    private BigDecimal leftoverInputCgst;
    private BigDecimal leftoverInputSgst;

    private String message;
}