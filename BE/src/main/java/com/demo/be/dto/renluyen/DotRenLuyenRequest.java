package com.demo.be.dto.renluyen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DotRenLuyenRequest(
        @NotBlank(message = "Tên đợt rèn luyện là bắt buộc") String tenDot,
        @NotBlank(message = "Học kỳ là bắt buộc") String hocKy,
        @NotBlank(message = "Năm học là bắt buộc") String namHoc,
        @NotNull(message = "Thời gian bắt đầu là bắt buộc") LocalDate ngayBatDau,
        @NotNull(message = "Thời gian kết thúc là bắt buộc") LocalDate ngayKetThuc,
        Boolean dangMo,
        String ghiChu
) {
}
