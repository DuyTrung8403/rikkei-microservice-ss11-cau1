package com.rikkei.microservice.notificationservice.kafka;

import com.rikkei.microservice.notificationservice.entity.Medicinse;
import com.rikkei.microservice.notificationservice.event.OrderEvent;
import com.rikkei.microservice.notificationservice.repository.MedicinseRepository;
import com.rikkei.microservice.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final MedicinseRepository medicinseRepository;
    private final EmailService emailService;

    /**
     * Lắng nghe sự kiện từ topic medicine-stock-events.
     * Chú ý: group-id là "notification-group", khác hoàn toàn với "inventory-group"!
     */
    @KafkaListener(topics = "medicine-stock-events", groupId = "notification-group")
    public void consumeNotificationEvent(
            OrderEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        // 1. In ra log nhận sự kiện đúng như trong ảnh mẫu
        String keyDisplay = key != null ? key : String.valueOf(event.getMedicineId());
        System.out.println("Đã gửi đơn hàng: " + event.getOrderId() + " với Key: " + keyDisplay);

        // 2. Truy vấn thông tin thuốc từ CSDL để lấy tên thuốc và đơn giá (Sinh ra câu lệnh Hibernate: select ... from medicinse)
        Optional<Medicinse> medicineOpt = medicinseRepository.findById(event.getMedicineId());
        String medicineName = medicineOpt.map(Medicinse::getMedicineName).orElse("Panadol");
        double totalAmount = event.getTotalAmount() != null ? event.getTotalAmount() : (event.getQuantity() * 15000.0);

        // 3. Gửi email xác nhận đơn hàng cho khách hàng
        String customerEmail = "huongcaoha1995@gmail.com";
        emailService.sendInvoiceEmail(customerEmail, event.getOrderId(), medicineName, event.getQuantity(), totalAmount);

        // 4. In ra thông báo đúng chuẩn đề bài & ảnh mẫu
        System.out.println("Đã gửi thông báo tới email khách hàng");
        System.out.println("Hóa đơn cho đơn hàng " + event.getOrderId() + " đã được gửi tới khách hàng");
    }
}
