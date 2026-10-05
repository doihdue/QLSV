package com.demo.be.controller;

import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.model.ThongBao;
import com.demo.be.producer.NotificationProducer;
import com.demo.be.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-bao")
@RequiredArgsConstructor
public class ThongBaoController {

    private final ThongBaoService thongBaoService;
    private final NotificationProducer notificationProducer;

    @GetMapping("/test-rabbitmq")
    public ResponseEntity<Map<String, Object>> testRabbitMQ() {
        NotificationEventMessage testMsg = NotificationEventMessage.builder()
                .eventType("TEST_MESSAGE")
                .tenMonHoc("Hệ thống Phân tán (RabbitMQ Test)")
                .maMonHoc("RABBITMQ01")
                .maLop("D19CNPM")
                .reason("Kiểm tra kết nối và nhận message qua RabbitMQ Queue thành công!")
                .build();

        notificationProducer.sendNotification(testMsg);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã gửi message test vào RabbitMQ Exchange [qlsv.exchange]. Hãy kiểm tra log server để thấy Consumer nhận message!",
                "data", testMsg
        ));
    }

    @GetMapping
    public ResponseEntity<List<ThongBao>> getMyNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(thongBaoService.getMyNotifications(principal.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("unreadCount", 0L));
        }
        long count = thongBaoService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            thongBaoService.markAsRead(id, principal.getName());
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        if (principal != null) {
            thongBaoService.markAllAsRead(principal.getName());
        }
        return ResponseEntity.noContent().build();
    }
}
