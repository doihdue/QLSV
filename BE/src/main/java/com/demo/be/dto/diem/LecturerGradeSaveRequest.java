package com.demo.be.dto.diem;

import java.math.BigDecimal;

public record LecturerGradeSaveRequest(
        Long dangKyMonHocId,
        BigDecimal diemChuyenCan,
        BigDecimal diemGiuaKy,
        BigDecimal diemCuoiKy
) {
}
