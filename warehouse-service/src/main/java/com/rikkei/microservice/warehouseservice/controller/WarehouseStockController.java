package com.rikkei.microservice.warehouseservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseStockController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseStockController.class);

    @GetMapping("/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(@RequestParam(defaultValue = "PANADOL") String medicineCode) {
        log.info(">> [WAREHOUSE-SERVICE] Đang xử lý kiểm tra tồn kho cho thuốc: {}", medicineCode);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "warehouse-service (Kho tổng)");
        response.put("medicineCode", medicineCode);
        response.put("stockQuantity", 500);
        response.put("status", "AVAILABLE");
        response.put("message", "Thuốc còn tồn 500 hộp trong kho tổng");

        return ResponseEntity.ok(response);
    }
}
