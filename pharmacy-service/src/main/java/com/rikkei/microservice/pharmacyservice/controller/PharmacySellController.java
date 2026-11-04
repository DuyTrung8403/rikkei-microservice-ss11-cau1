package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.event.OrderEvent;
import com.rikkei.microservice.pharmacyservice.kafka.PharmacyOrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacySellController {

    private final PharmacyOrderProducer orderProducer;

    /**
     * API bán thuốc: POST /api/pharmacy/sell?medicineId=1&quantity=5
     * Khớp 100% với ảnh mẫu đề bài!
     */
    @PostMapping("/sell")
    public ResponseEntity<String> sellMedicine(
            @RequestParam(defaultValue = "1") Long medicineId,
            @RequestParam(defaultValue = "5") Integer quantity) {

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double priceSell = 10000.0;
        double totalAmount = priceSell * quantity;

        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(orderId)
                .medicineId(medicineId)
                .quantity(quantity)
                .priceSell(priceSell)
                .totalAmount(totalAmount)
                .timestamp(LocalDateTime.now())
                .build();

        // Gửi sự kiện lên Kafka Broker với Key = medicineId
        orderProducer.sendOrderEvent(orderEvent);

        // Trả về chuỗi thông báo đúng y hệt trong ảnh Postman của đề bài
        return ResponseEntity.ok("Thanh toán thành công! Sự kiện đã được gửi tới Kafka.");
    }
}
