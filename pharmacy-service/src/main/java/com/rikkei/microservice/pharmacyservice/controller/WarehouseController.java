package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.service.WarehouseClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseClientService warehouseClientService;

    /**
     * Endpoint kiểm tra tồn kho.
     * GET http://localhost:8081/api/v1/warehouse/check-stock?medicineCode=PANADOL
     */
    @GetMapping("/check-stock")
    public ResponseEntity<String> checkStock(
            @RequestParam(defaultValue = "PANADOL") String medicineCode) {
        String result = warehouseClientService.checkWarehouse(medicineCode);
        return ResponseEntity.ok(result);
    }
}
