package com.rikkei.microservice.inventoryservice;

import com.rikkei.microservice.inventoryservice.entity.Medicinse;
import com.rikkei.microservice.inventoryservice.repository.MedicinseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    /**
     * Tự động khởi tạo dữ liệu mẫu thuốc có id = 1 để test trừ tồn kho
     */
    @Bean
    public CommandLineRunner initData(MedicinseRepository medicinseRepository) {
        return args -> {
            if (medicinseRepository.count() == 0) {
                Medicinse medicine = Medicinse.builder()
                        .medicineName("Panadol Extra")
                        .description("Thuốc giảm đau hạ sốt")
                        .price(10000.0)
                        .quantity(100)
                        .build();
                medicinseRepository.save(medicine);
                System.out.println(">> Đã khởi tạo dữ liệu mẫu thuốc ID = 1, Số lượng kho = 100");
            }
        };
    }
}
