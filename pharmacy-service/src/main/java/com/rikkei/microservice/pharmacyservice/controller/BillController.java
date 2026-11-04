package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.dto.CheckoutRequest;
import com.rikkei.microservice.pharmacyservice.event.OrderEvent;
import com.rikkei.microservice.pharmacyservice.kafka.PharmacyOrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bill")
@RefreshScope
public class BillController {

    @Value("${pharmacy.vat-rate:0.08}")
    private double vatRate;

    @Autowired
    private PharmacyOrderProducer orderProducer;

    /**
     * API 1: Tính tiền hóa đơn thuốc (Dùng cho bài RefreshScope trước)
     */
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

    /**
     * API 2: Thanh toán đơn thuốc và gửi sự kiện OrderEvent lên Kafka Broker (Topic: medicine-stock-events)
     * Thử thách: Key của message là medicineId để cùng loại thuốc luôn vào cùng 1 Partition.
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        String medicineId = request.getMedicineId() != null ? request.getMedicineId() : "MED-" + UUID.randomUUID().toString().substring(0, 5);
        int quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        double price = request.getPrice() != null && request.getPrice() > 0 ? request.getPrice() : 50000.0;

        // Tính tiền thuốc kèm VAT
        double subtotal = price * quantity;
        double rate = vatRate >= 1 ? vatRate / 100.0 : vatRate;
        double vatAmount = subtotal * rate;
        double totalAmount = subtotal + vatAmount;

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Long medId = 1L;
        try {
            if (request.getMedicineId() != null) {
                medId = Long.parseLong(request.getMedicineId().replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {
        }

        // 1. Tạo đối tượng OrderEvent theo yêu cầu: orderId, medicineId, quantity, timestamp
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(orderId)
                .medicineId(medId)
                .quantity(quantity)
                .priceSell(price)
                .totalAmount(totalAmount)
                .timestamp(LocalDateTime.now())
                .build();

        // 2. Gửi sự kiện lên Kafka Broker (Message Key = medicineId)
        orderProducer.sendOrderEvent(orderEvent);

        // 3. Phản hồi thông tin thanh toán thành công
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Thanh toán thành công! Sự kiện đơn hàng đã được gửi lên Kafka Broker.");
        response.put("orderId", orderId);
        response.put("medicineId", medicineId);
        response.put("quantity", quantity);
        response.put("subtotal", subtotal);
        response.put("vatAmount", vatAmount);
        response.put("totalPayment", totalAmount);
        response.put("kafkaTopic", PharmacyOrderProducer.TOPIC);
        response.put("kafkaMessageKey", medicineId);
        response.put("partitionGuarantee", "Các đơn hàng cùng medicineId '" + medicineId + "' luôn vào cùng 1 Partition!");

        return ResponseEntity.ok(response);
    }
}
