package com.demo.be.dto.report;

import java.time.LocalDateTime;

public record ReportJobResponse(
        String jobId,
        String reportType,
        String title,
        String status,
        String fileName,
        Long fileSize,
        String requestedBy,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        String errorMessage
) {
}
