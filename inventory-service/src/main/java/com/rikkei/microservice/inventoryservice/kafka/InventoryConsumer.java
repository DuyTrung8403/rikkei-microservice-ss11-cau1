package com.rikkei.microservice.inventoryservice.kafka;

import com.rikkei.microservice.inventoryservice.entity.Order;
import com.rikkei.microservice.inventoryservice.event.OrderEvent;
import com.rikkei.microservice.inventoryservice.repository.MedicinseRepository;
import com.rikkei.microservice.inventoryservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    private final OrderRepository orderRepository;
    private final MedicinseRepository medicinseRepository;

    /**
     * Lắng nghe sự kiện từ topic medicine-stock-events.
     * Cấu hình groupId = "inventory-group" để đảm bảo tính nhất quán (không xử lý trùng lặp trong cụm).
     */
    @KafkaListener(topics = "medicine-stock-events", groupId = "inventory-group")
    @Transactional
    public void consumeOrderEvent(OrderEvent event) {
        log.info(">> [INVENTORY CONSUMER] Nhận sự kiện đơn hàng: medicineId={}, quantity={}",
                event.getMedicineId(), event.getQuantity());

        // 1. Thêm mới đơn hàng vào bảng 'orders'
        Order order = Order.builder()
                .medicineId(event.getMedicineId())
                .priceSell(event.getPriceSell() != null ? event.getPriceSell() : 10000.0)
                .quantity(event.getQuantity())
                .totalAmount(event.getTotalAmount() != null ? event.getTotalAmount() : (event.getQuantity() * 10000.0))
                .timestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                .build();
        orderRepository.save(order);

        // 2. Tìm thuốc trong bảng 'medicinse' và trừ số lượng tồn kho (UPDATE medicinse SET quantity = quantity - event.quantity)
        medicinseRepository.findById(event.getMedicineId()).ifPresentOrElse(medicinse -> {
            int oldQuantity = medicinse.getQuantity() != null ? medicinse.getQuantity() : 0;
            int newQuantity = Math.max(0, oldQuantity - event.getQuantity());
            medicinse.setQuantity(newQuantity);
            medicinseRepository.save(medicinse);
            log.info(">> Đã cập nhật tồn kho thuốc '{}': {} -> {}", medicinse.getMedicineName(), oldQuantity, newQuantity);
        }, () -> {
            log.warn(">> Không tìm thấy thuốc có id = {} để trừ kho", event.getMedicineId());
        });

        // 3. In ra thông báo thành công đúng y hệt mẫu đề bài
        System.out.println("Thêm mới đơn hàng thành công !");
    }
}
