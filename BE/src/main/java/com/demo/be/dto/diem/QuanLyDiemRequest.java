package com.demo.be.dto.diem;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuanLyDiemRequest(
        @NotNull(message = "Dang ky mon hoc id is required") Long dangKyMonHocId,
        @DecimalMin(value = "0.0", message = "Diem chuyen can must be at least 0.0")
        @DecimalMax(value = "10.0", message = "Diem chuyen can must be at most 10.0") BigDecimal diemChuyenCan,
        @DecimalMin(value = "0.0", message = "Diem giua ky must be at least 0.0")
        @DecimalMax(value = "10.0", message = "Diem giua ky must be at most 10.0") BigDecimal diemGiuaKy,
        @DecimalMin(value = "0.0", message = "Diem cuoi ky must be at least 0.0")
        @DecimalMax(value = "10.0", message = "Diem cuoi ky must be at most 10.0") BigDecimal diemCuoiKy
) {
}
