package com.demo.be.consumer;

import com.demo.be.config.RabbitMQConfig;
import com.demo.be.dto.report.ReportExportMessage;
import com.demo.be.service.ReportJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReportConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReportConsumer.class);

    private final ReportJobService reportJobService;

    public ReportConsumer(ReportJobService reportJobService) {
        this.reportJobService = reportJobService;
    }

    @RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE_NAME)
    public void consumeReportExport(ReportExportMessage message) {
        log.info("[RABBITMQ REPORT CONSUMER] <=== Nhận message xuất báo cáo từ Queue [{}]: jobId={}, type={}",
                RabbitMQConfig.REPORT_QUEUE_NAME, message.jobId(), message.reportType());
        try {
            reportJobService.processJob(message);
        } catch (Exception e) {
            log.error("[RABBITMQ REPORT CONSUMER] Lỗi khi xử lý xuất báo cáo jobId={}: {}", message.jobId(), e.getMessage(), e);
        }
    }
}
