package com.rikkei.microservice.pharmacyservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bill")
@RefreshScope
public class BillController {

    @Value("${pharmacy.vat-rate:0.08}")
    private double vatRate;

    @PostMapping
    public ResponseEntity<?> calculateBill(@RequestBody Map<String, Double> request) {
        Double totalMedicinePrice = request.get("totalMedicinePrice");
        if (totalMedicinePrice == null) {
            totalMedicinePrice = request.get("subtotal");
        }
        if (totalMedicinePrice == null) {
            totalMedicinePrice = request.get("amount");
        }

        if (totalMedicinePrice == null || totalMedicinePrice < 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Vui lòng nhập tổng tiền thuốc hợp lệ (key: 'totalMedicinePrice' hoặc 'subtotal')"
            ));
        }

        // Tỷ lệ thuế: hỗ trợ cả dạng thập phân (0.08) và phần trăm (8 hoặc 10)
        double rate = vatRate >= 1 ? vatRate / 100.0 : vatRate;
        double vatAmount = totalMedicinePrice * rate;
        double totalBill = totalMedicinePrice + vatAmount;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMedicinePrice", totalMedicinePrice);
        result.put("vatRateConfigured", vatRate);
        result.put("vatAmount", vatAmount);
        result.put("totalBill", totalBill);
        result.put("message", "Tính tiền hóa đơn thuốc thành công");

        return ResponseEntity.ok(result);
    }
}
