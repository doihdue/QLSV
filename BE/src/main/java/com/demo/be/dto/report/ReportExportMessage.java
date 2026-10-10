package com.demo.be.dto.report;

public record ReportExportMessage(
        String jobId,
        String reportType,
        Long dotId,
        Long lopId,
        String format,
        String requestedBy
) {
}
