package com.demo.be.producer;

import com.demo.be.config.RabbitMQConfig;
import com.demo.be.dto.report.ReportExportMessage;
import com.demo.be.service.ReportJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ReportExportProducer {

    private static final Logger log = LoggerFactory.getLogger(ReportExportProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ReportJobService reportJobService;

    public ReportExportProducer(RabbitTemplate rabbitTemplate, @Lazy ReportJobService reportJobService) {
        this.rabbitTemplate = rabbitTemplate;
        this.reportJobService = reportJobService;
    }

    public void sendExportMessage(ReportExportMessage message) {
        log.info("[RABBITMQ REPORT PRODUCER] ===> Đẩy yêu cầu xuất báo cáo [{}] jobId=[{}] vào Exchange: {}",
                message.reportType(), message.jobId(), RabbitMQConfig.EXCHANGE_NAME);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_REPORT,
                    message
            );
            log.info("[RABBITMQ REPORT PRODUCER] ===> Đã chuyển giao nhiệm vụ xuất báo cáo thành công tới RabbitMQ queue: {}",
                    RabbitMQConfig.REPORT_QUEUE_NAME);
        } catch (AmqpException e) {
            log.warn("[RABBITMQ REPORT PRODUCER] [FALLBACK ASYNC] Không thể kết nối tới RabbitMQ (5672): {}. Tự động chuyển sang xử lý ngầm trong thread riêng...", e.getMessage());
            // Fallback an toàn: chạy ngầm trên CompletableFuture để không làm đơ trang / blocking request
            CompletableFuture.runAsync(() -> reportJobService.processJob(message));
        }
    }
}
