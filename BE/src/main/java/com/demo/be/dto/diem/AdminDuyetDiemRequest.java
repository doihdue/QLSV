package com.demo.be.dto.diem;

public record AdminDuyetDiemRequest(
        String ghiChu,
        String reason
) {
    public String getEffectiveReason() {
        if (ghiChu != null && !ghiChu.isBlank()) return ghiChu;
        if (reason != null && !reason.isBlank()) return reason;
        return null;
    }
}

