package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.entity.Medicine;
import com.rikkei.microservice.pharmacyservice.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    /**
     * API lấy chi tiết thuốc theo ID để test Redis Cache:
     * GET http://localhost:8081/api/medicines/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMedicine(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();

        Medicine medicine = medicineService.getMedicineById(id);

        long responseTime = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("medicine", medicine);
        response.put("responseTimeMs", responseTime + " ms");
        response.put("note", responseTime < 20 ? "Lấy từ Redis Cache (Cực nhanh)" : "Lấy từ Database PostgreSQL (Lần đầu)");

        return ResponseEntity.ok(response);
    }
}
