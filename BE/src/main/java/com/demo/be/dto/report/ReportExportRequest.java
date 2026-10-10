package com.demo.be.dto.report;

import jakarta.validation.constraints.NotNull;

public record ReportExportRequest(
        @NotNull(message = "Đợt rèn luyện không được để trống") Long dotId,
        @NotNull(message = "Lớp hành chính không được để trống") Long lopId,
        String format // Default "PDF"
) {
}
