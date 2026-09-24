package com.demo.be.dto.dangky;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record DotDangKyRequest(
        @NotBlank(message = "Học kỳ là bắt buộc") String hocKy,
        @NotBlank(message = "Năm học là bắt buộc") String namHoc,
        String tenDot,
        Boolean dangMo,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc
) {
}
