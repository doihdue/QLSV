package com.demo.be.dto.dangky;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentDangKyMonHocRequest(
        @NotNull(message = "Mã môn học là bắt buộc") Long monHocId,
        @NotBlank(message = "Học kỳ là bắt buộc") @Size(max = 20, message = "Học kỳ tối đa 20 ký tự") String hocKy,
        @NotBlank(message = "Năm học là bắt buộc") @Size(max = 20, message = "Năm học tối đa 20 ký tự") String namHoc
) {
}
