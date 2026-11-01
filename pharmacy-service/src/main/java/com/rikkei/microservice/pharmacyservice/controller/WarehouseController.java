package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.service.WarehouseClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseClientService warehouseClientService;

    /**
     * Endpoint kiểm tra tồn kho từ kho tổng (warehouse-service).
     * Ví dụ: GET http://localhost:8081/api/v1/warehouse/check-stock?medicineCode=PANADOL
     */
    @GetMapping("/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(
            @RequestParam(defaultValue = "PANADOL") String medicineCode) {
        Map<String, Object> result = warehouseClientService.checkStock(medicineCode);
        return ResponseEntity.ok(result);
    }
}
