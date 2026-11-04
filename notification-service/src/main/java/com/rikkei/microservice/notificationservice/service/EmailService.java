package com.rikkei.microservice.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    /**
     * Gửi hóa đơn điện tử về email cho khách hàng
     */
    public void sendInvoiceEmail(String toEmail, String orderId, String medicineName, int quantity, double totalAmount) {
        String subject = "Xác nhận đơn hàng";
        String content = "Cảm ơn bạn đã đặt hàng tại Pharma Medicine:\n"
                + "Tên thuốc : " + medicineName + "\n"
                + "Số lượng : " + quantity + "\n"
                + "Tổng tiền : " + String.format("%.6f", totalAmount);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info(">> Email thực tế đã được gửi thành công đến: {}", toEmail);
        } catch (Exception ex) {
            // Khi chưa cấu hình mật khẩu email thật, ghi nhận và mô phỏng email thành công
            log.warn(">> [MÔ PHỎNG EMAIL] Gửi email qua SMTP gặp lỗi ({}), mô phỏng gửi thành công:", ex.getMessage());
            System.out.println("--------------------------------------------------");
            System.out.println("GỬI EMAIL: " + subject);
            System.out.println("Tới: " + toEmail);
            System.out.println(content);
            System.out.println("--------------------------------------------------");
        }
    }
}
