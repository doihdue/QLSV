package com.demo.be.dto.lop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LopRequest(
        @Size(max = 20, message = "Mã lớp tối đa 20 ký tự") String maLop,
        @NotBlank(message = "Tên lớp hành chính là bắt buộc") @Size(max = 150, message = "Tên lớp tối đa 150 ký tự") String tenLop,
        @NotBlank(message = "Niên khóa là bắt buộc") @Size(max = 20, message = "Niên khóa tối đa 20 ký tự") String nienKhoa,
        @NotNull(message = "Sĩ số tối đa là bắt buộc") @Positive(message = "Sĩ số tối đa phải lớn hơn 0") Integer siSoToiDa,
        @NotNull(message = "Vui lòng chọn Khoa trực thuộc") Long khoaId,
        Boolean active
) {
}
