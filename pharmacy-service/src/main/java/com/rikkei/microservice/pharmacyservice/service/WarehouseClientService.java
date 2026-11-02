package com.rikkei.microservice.pharmacyservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WarehouseClientService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseClientService.class);
    private final RestTemplate restTemplate;

    public WarehouseClientService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3s timeout kết nối
        factory.setReadTimeout(3000);    // 3s timeout đọc dữ liệu
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Hàm gọi kiểm tra tồn kho sang warehouse-service.
     * Đăng ký fallbackMethod = "checkWarehouseFallback".
     */
    @CircuitBreaker(name = "warehouseCB", fallbackMethod = "checkWarehouseFallback")
    public String checkWarehouse(String medicineCode) {
        log.info(">> Đang gọi sang warehouse-service (http://localhost:8082) kiểm tra thuốc: {}", medicineCode);
        String url = "http://localhost:8082/api/warehouse/check-stock?medicineCode=" + medicineCode;
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * Fallback method đúng theo yêu cầu đề bài:
     * Khi kho tổng bị lỗi hoặc mạch ngắt OPEN, trả về thông báo sử dụng kho cục bộ thay vì lỗi 500.
     */
    public String checkWarehouseFallback(String medicineCode, Exception e) {
        log.warn(">> [CIRCUIT BREAKER TRIGGERED] Lỗi kết nối kho tổng: {}", e.getMessage());
        return "Không thể kết nối kho tổng. Hệ thống sẽ sử dụng dữ liệu tồn kho cục bộ để tiếp tục giao dịch";
    }

    /**
     * Hàm fallback overload (nhận Exception e) đúng từng chữ theo đặc tả đề bài
     */
    public String checkWarehouseFallback(Exception e) {
        log.warn(">> [CIRCUIT BREAKER TRIGGERED] Lỗi kết nối kho tổng: {}", e.getMessage());
        return "Không thể kết nối kho tổng. Hệ thống sẽ sử dụng dữ liệu tồn kho cục bộ để tiếp tục giao dịch";
    }
}
