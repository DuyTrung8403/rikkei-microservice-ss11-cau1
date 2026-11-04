package com.rikkei.microservice.pharmacyservice.kafka;

import com.rikkei.microservice.pharmacyservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PharmacyOrderProducer {

    private static final Logger log = LoggerFactory.getLogger(PharmacyOrderProducer.class);
    public static final String TOPIC = "medicine-stock-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Gửi OrderEvent lên Kafka Broker.
     * Sử dụng Message Key là medicineId để đảm bảo:
     * Các đơn hàng của cùng một loại thuốc luôn đi vào cùng một Partition!
     */
    public CompletableFuture<SendResult<String, Object>> sendOrderEvent(OrderEvent orderEvent) {
        String key = orderEvent.getMedicineId();
        log.info(">> [KAFKA PRODUCER] Đang gửi OrderEvent: topic='{}', key (medicineId)='{}', orderId='{}'",
                TOPIC, key, orderEvent.getOrderId());

        return kafkaTemplate.send(TOPIC, key, orderEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(">> [KAFKA SUCCESS] Gửi thành công lên Kafka Broker! Partition: {}, Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error(">> [KAFKA ERROR] Gửi tin nhắn thất bại: {}", ex.getMessage());
                    }
                });
    }
}
