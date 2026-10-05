package com.demo.be.dto.monhoc;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MonHocRequest(
        @NotBlank(message = "Mã môn học là bắt buộc") @Size(max = 20, message = "Mã môn học tối đa 20 ký tự") String maMonHoc,
        @NotBlank(message = "Tên môn học là bắt buộc") @Size(max = 150, message = "Tên môn học tối đa 150 ký tự") String tenMonHoc,
        @NotNull(message = "Số tín chỉ là bắt buộc") @Min(value = 1, message = "Số tín chỉ phải từ 1 trở lên") Integer soTinChi,
        @NotNull(message = "Số tiết lý thuyết là bắt buộc") @Min(value = 0, message = "Số tiết lý thuyết không được âm") Integer soTietLyThuyet,
        @NotNull(message = "Số tiết thực hành là bắt buộc") @Min(value = 0, message = "Số tiết thực hành không được âm") Integer soTietThucHanh,
        @Size(max = 500, message = "Mô tả tối đa 500 ký tự") String moTa,
        @Size(max = 255, message = "Môn học tiên quyết tối đa 255 ký tự") String monHocTienQuyet,
        @NotNull(message = "Vui lòng chọn Khoa phụ trách") Long khoaId,
        Boolean active
) {
}
