package com.inventory.gst_billing.service;

import com.inventory.gst_billing.dto.GstCalculationResponse;
import com.inventory.gst_billing.entity.GstLedger;
import com.inventory.gst_billing.entity.Store;
import com.inventory.gst_billing.enums.GstComponent;
import com.inventory.gst_billing.enums.GstType;
import com.inventory.gst_billing.repository.GstLedgerRepository;
import com.inventory.gst_billing.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GstCalculationService {

    private final GstLedgerRepository gstLedgerRepo;
    private final StoreRepository storeRepo;

    public GstCalculationService(GstLedgerRepository gstLedgerRepo, StoreRepository storeRepo) {
        this.gstLedgerRepo = gstLedgerRepo;
        this.storeRepo = storeRepo;
    }


    // ENDPOINT 1: THE REAL-TIME DASHBOARD (Uses Lifetime Data)
    @Transactional(readOnly = true)
    public GstCalculationResponse calculateGstOffset(String stateCode) {
        // find a list of all teh stores in the given state of code
        List<Store> stateStores = storeRepo.findByStateCode(stateCode.toUpperCase());
        if (stateStores.isEmpty()) throw new RuntimeException("No stores found in state: " + stateCode);

        // instead of a list of stores we need list of storeids of the stores in that state
        List<Integer> storeIds = stateStores.stream().map(Store::getId).collect(Collectors.toList());
        // find ledger entries of all the stores in that state and combien to create virtual ledger of that sstea
        List<GstLedger> ledgerEntries = gstLedgerRepo.findByStoreIdIn(storeIds);

        // calculation algorithm run to get back response/
        GstCalculationResponse response = runOffsetAlgorithm(ledgerEntries);
        response.setStateCode(stateCode.toUpperCase());
        return response;
    }


    // ENDPOINT 2: PERIOD SETTLEMENT (Closes the Month & Carries Forward)
    @Transactional
    public String settleMonth(String stateCode, int year, int month) {

        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime nextMonthStart = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Store> stores = storeRepo.findByStateCode(stateCode.toUpperCase());
        List<Integer> storeIds = stores.stream().map(Store::getId).collect(Collectors.toList());

        // 1. Fetch ONLY this month's data (including any carry-forwards from last month)
        List<GstLedger> monthEntries = gstLedgerRepo.findByStoreIdInAndCreatedAtBetween(storeIds, start, end);

        // 2. run the  Math logic
        GstCalculationResponse math = runOffsetAlgorithm(monthEntries);

        Integer primaryStoreId = storeIds.get(0); // Use the first store as the anchor for state-wide settlements

        // 3. wipe out this months totals, double accounting lgic
        logLedger(math.getTotalOutputIgst().negate(), GstComponent.IGST, GstType.OUTPUT, "SETTLEMENT", primaryStoreId, end);
        logLedger(math.getTotalOutputCgst().negate(), GstComponent.CGST, GstType.OUTPUT, "SETTLEMENT", primaryStoreId, end);
        logLedger(math.getTotalOutputSgst().negate(), GstComponent.SGST, GstType.OUTPUT, "SETTLEMENT", primaryStoreId, end);

        logLedger(math.getTotalInputIgst().negate(), GstComponent.IGST, GstType.INPUT, "SETTLEMENT", primaryStoreId, end);
        logLedger(math.getTotalInputCgst().negate(), GstComponent.CGST, GstType.INPUT, "SETTLEMENT", primaryStoreId, end);
        logLedger(math.getTotalInputSgst().negate(), GstComponent.SGST, GstType.INPUT, "SETTLEMENT", primaryStoreId, end);

        // 4. carry forward left over to next motnh
        logLedger(math.getLeftoverInputIgst(), GstComponent.IGST, GstType.INPUT, "OPENING_BALANCE", primaryStoreId, nextMonthStart);
        logLedger(math.getLeftoverInputCgst(), GstComponent.CGST, GstType.INPUT, "OPENING_BALANCE", primaryStoreId, nextMonthStart);
        logLedger(math.getLeftoverInputSgst(), GstComponent.SGST, GstType.INPUT, "OPENING_BALANCE", primaryStoreId, nextMonthStart);

        return "Month " + month + "/" + year + " closed successfully. Leftover ITC carried forward!";
    }

    // THE CORE GST CALCULATION ALGO RUN BY BOTH METHODS ABOVE.
    private GstCalculationResponse runOffsetAlgorithm(List<GstLedger> ledgerEntries) {
        // 1. Variables for storing for Input (Credit) and Output (Liability)
        // Will loop throuhg all the ledger entries adding each ocmpoennt in their respective variable
        BigDecimal inIgst = BigDecimal.ZERO, inCgst = BigDecimal.ZERO, inSgst = BigDecimal.ZERO;
        BigDecimal outIgst = BigDecimal.ZERO, outCgst = BigDecimal.ZERO, outSgst = BigDecimal.ZERO;

        // 2. Add all ledger entries into their respective varaible bucket.
        for (GstLedger entry : ledgerEntries) {
            if (entry.getType() == GstType.INPUT) {
                if (entry.getComponent() == GstComponent.IGST) inIgst = inIgst.add(entry.getAmount());
                else if (entry.getComponent() == GstComponent.CGST) inCgst = inCgst.add(entry.getAmount());
                else if (entry.getComponent() == GstComponent.SGST) inSgst = inSgst.add(entry.getAmount());
            } else {
                if (entry.getComponent() == GstComponent.IGST) outIgst = outIgst.add(entry.getAmount());
                else if (entry.getComponent() == GstComponent.CGST) outCgst = outCgst.add(entry.getAmount());
                else if (entry.getComponent() == GstComponent.SGST) outSgst = outSgst.add(entry.getAmount());
            }
        }

        // 3. Set up Payable Variables
        //Start by assuming we owe the full amount
        BigDecimal payIgst = outIgst;
        BigDecimal payCgst = outCgst;
        BigDecimal paySgst = outSgst;

        BigDecimal remInIgst = inIgst;
        BigDecimal remInCgst = inCgst;
        BigDecimal remInSgst = inSgst;

        // --- RULE 1: OFFSET IGST CREDIT ---
        // 1a. IGST Credit pays IGST Liability
        BigDecimal offset = payIgst.min(remInIgst);
        payIgst = payIgst.subtract(offset);
        remInIgst = remInIgst.subtract(offset);

        // 1b. Leftover IGST Credit pays CGST Liability
        offset = payCgst.min(remInIgst);
        payCgst = payCgst.subtract(offset);
        remInIgst = remInIgst.subtract(offset);

        // 1c. Leftover IGST Credit pays SGST Liability
        offset = paySgst.min(remInIgst);
        paySgst = paySgst.subtract(offset);
        remInIgst = remInIgst.subtract(offset);

        // --- RULE 2: OFFSET CGST CREDIT ---
        // 2a. CGST Credit pays CGST Liability
        offset = payCgst.min(remInCgst);
        payCgst = payCgst.subtract(offset);
        remInCgst = remInCgst.subtract(offset);

        // 2b. Leftover CGST Credit pays IGST Liability (CANNOT pay SGST)
        offset = payIgst.min(remInCgst);
        payIgst = payIgst.subtract(offset);
        remInCgst = remInCgst.subtract(offset);

        // --- RULE 3: OFFSET SGST CREDIT ---
        // 3a. SGST Credit pays SGST Liability
        offset = paySgst.min(remInSgst);
        paySgst = paySgst.subtract(offset);
        remInSgst = remInSgst.subtract(offset);

        // 3b. Leftover SGST Credit pays IGST Liability (CANNOT pay CGST)
        offset = payIgst.min(remInSgst);
        payIgst = payIgst.subtract(offset);
        remInSgst = remInSgst.subtract(offset);

        // Build Response
        GstCalculationResponse response = new GstCalculationResponse();
        response.setTotalInputIgst(inIgst); response.setTotalInputCgst(inCgst); response.setTotalInputSgst(inSgst);
        response.setTotalOutputIgst(outIgst); response.setTotalOutputCgst(outCgst); response.setTotalOutputSgst(outSgst);
        response.setNetPayableIgst(payIgst); response.setNetPayableCgst(payCgst); response.setNetPayableSgst(paySgst);

        // Save the leftovers so the Settlement method can grab them!
        response.setLeftoverInputIgst(remInIgst);
        response.setLeftoverInputCgst(remInCgst);
        response.setLeftoverInputSgst(remInSgst);

        response.setMessage("GST Offset Calculation applied successfully according to priority rules.");
        return response;
    }

    // HELPER: Ledger Logger
    private void logLedger(BigDecimal amount, GstComponent comp, GstType type, String transType, Integer storeId, LocalDateTime date) {
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            GstLedger ledger = new GstLedger();
            ledger.setComponent(comp);
            ledger.setType(type);
            ledger.setAmount(amount);
            ledger.setTransactionId(0); // System generated, no linked invoice
            ledger.setTransactionType(transType);
            ledger.setCreatedAt(date);

            Store store = new Store();
            store.setId(storeId);
            ledger.setStore(store);

            gstLedgerRepo.save(ledger);
        }
    }
}