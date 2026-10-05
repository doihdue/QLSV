package com.demo.be.dto.dangky;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DotDangKyRequest(
        @NotBlank(message = "Học kỳ là bắt buộc") String hocKy,
        @NotBlank(message = "Năm học là bắt buộc") String namHoc,
        @NotBlank(message = "Tên đợt đăng ký là bắt buộc") String tenDot,
        Boolean dangMo,
        @NotNull(message = "Thời gian bắt đầu mở đợt là bắt buộc") LocalDate ngayBatDau,
        @NotNull(message = "Thời gian kết thúc đợt là bắt buộc") LocalDate ngayKetThuc
) {
}
