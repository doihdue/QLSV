package com.demo.be.dto.diem;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuanLyDiemRequest(
        @NotNull(message = "Mã đăng ký môn học là bắt buộc") Long dangKyMonHocId,
        @DecimalMin(value = "0.0", message = "Điểm chuyên cần phải từ 0.0 trở lên")
        @DecimalMax(value = "10.0", message = "Điểm chuyên cần tối đa là 10.0") BigDecimal diemChuyenCan,
        @DecimalMin(value = "0.0", message = "Điểm giữa kỳ phải từ 0.0 trở lên")
        @DecimalMax(value = "10.0", message = "Điểm giữa kỳ tối đa là 10.0") BigDecimal diemGiuaKy,
        @DecimalMin(value = "0.0", message = "Điểm cuối kỳ phải từ 0.0 trở lên")
        @DecimalMax(value = "10.0", message = "Điểm cuối kỳ tối đa là 10.0") BigDecimal diemCuoiKy
) {
}
