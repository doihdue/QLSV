package com.demo.be.consumer;

import com.demo.be.config.RabbitMQConfig;
import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ThongBaoService thongBaoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveNotificationMessage(NotificationEventMessage event) {
        log.info("[RABBITMQ CONSUMER] <=== Đã nhận được message từ Queue [{}]: Sự kiện = {}, Môn = {}, Lớp = {}",
                RabbitMQConfig.QUEUE_NAME, event.getEventType(), event.getTenMonHoc(), event.getMaLop());

        thongBaoService.processNotificationEvent(event);

        log.info("[RABBITMQ CONSUMER] <=== Đã xử lý xong thông báo và ghi nhận vào CSDL thành công!");
    }
}
