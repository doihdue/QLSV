package com.demo.be.dto.monhocmo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MonHocMoRequest(
        @NotNull(message = "Môn học là bắt buộc")
        Long monHocId,

        @NotNull(message = "Khoa là bắt buộc")
        Long khoaId,

        @NotBlank(message = "Khóa học là bắt buộc (ví dụ: D22, D23...)")
        String khoaHoc,

        @NotBlank(message = "Học kỳ là bắt buộc")
        String hocKy,

        @NotBlank(message = "Năm học là bắt buộc")
        String namHoc,

        Long lopId, // Tùy chọn

        Long giangVienId, // Giáo viên bộ môn phụ trách

        String ghiChu
) {
}
