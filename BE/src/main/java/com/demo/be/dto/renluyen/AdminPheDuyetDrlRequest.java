package com.demo.be.dto.renluyen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminPheDuyetDrlRequest(
        @NotNull(message = "ID phiếu rèn luyện là bắt buộc") Long bangDiemId,
        @NotBlank(message = "Hành động duyệt là bắt buộc (APPROVE hoặc REJECT)") String action,
        String nhanXetAdmin,
        Integer diemDieuChinh
) {
}
