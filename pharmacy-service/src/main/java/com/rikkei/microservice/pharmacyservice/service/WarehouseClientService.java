package com.rikkei.microservice.pharmacyservice.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WarehouseClientService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseClientService.class);
    private final RestTemplate restTemplate;

    public WarehouseClientService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3 giây timeout kết nối
        factory.setReadTimeout(3000);    // 3 giây timeout đọc dữ liệu
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Hàm gọi HTTP sang microservice riêng biệt: warehouse-service (Port 8082).
     * Được bảo vệ bởi @CircuitBreaker với name = "warehouseCB" (cấu hình trên Git).
     *
     * @param medicineCode Mã thuốc cần kiểm tra tồn kho
     */
    @CircuitBreaker(name = "warehouseCB", fallbackMethod = "checkWarehouseStockFallback")
    public Map<String, Object> checkStock(String medicineCode) {
        log.info(">> Đang gọi HTTP sang warehouse-service (http://localhost:8082) cho thuốc: {}", medicineCode);

        // Gọi HTTP thực sự sang module warehouse-service đang chạy ở port 8082
        String url = "http://localhost:8082/api/warehouse/check-stock?medicineCode=" + medicineCode;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "SUCCESS");
        result.put("circuitState", "CLOSED");
        result.put("warehouseResponse", response.getBody());
        result.put("message", "Kiểm tra tồn kho thành công từ kho tổng warehouse-service ✓");
        return result;
    }

    /**
     * Fallback method khi gọi warehouse-service thất bại hoặc khi mạch đã OPEN.
     */
    public Map<String, Object> checkWarehouseStockFallback(String medicineCode, Throwable throwable) {
        Map<String, Object> fallbackResponse = new LinkedHashMap<>();
        fallbackResponse.put("medicineCode", medicineCode);

        if (throwable instanceof CallNotPermittedException) {
            // Khi mạch đã OPEN: Từ chối gọi ngay lập tức (Fail-Fast), bảo vệ máy tính tiền
            log.warn(">> [FAIL-FAST] MẠCH ĐANG OPEN! Từ chối gọi warehouse-service để tránh treo máy.");
            fallbackResponse.put("status", "CIRCUIT_OPEN");
            fallbackResponse.put("circuitState", "OPEN");
            fallbackResponse.put("failFast", true);
            fallbackResponse.put("message", "Mạch warehouseCB đang OPEN: Kho tổng đang sập hoặc phản hồi chậm! Mạch đã tự động ngắt (20s) để tránh treo máy bán lẻ ⛔");
        } else {
            // Khi mạch vẫn CLOSED nhưng lần gọi này bị lỗi kết nối
            log.error(">> [CALL FAILED] Lỗi gọi kho tổng: {}", throwable.getMessage());
            fallbackResponse.put("status", "CALL_FAILED");
            fallbackResponse.put("circuitState", "CLOSED (Đang tích lũy lỗi)");
            fallbackResponse.put("failFast", false);
            fallbackResponse.put("error", throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
            fallbackResponse.put("message", "Không thể kết nối đến warehouse-service (Kho tổng).");
        }

        return fallbackResponse;
    }
}
