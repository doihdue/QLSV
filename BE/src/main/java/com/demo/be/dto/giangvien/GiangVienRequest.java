package com.demo.be.dto.giangvien;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GiangVienRequest(
        @Size(max = 20, message = "Mã giảng viên tối đa 20 ký tự") String maGiangVien,
        @NotBlank(message = "Họ và tên giảng viên là bắt buộc") @Size(max = 150, message = "Họ và tên tối đa 150 ký tự") String hoTen,
        @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String soDienThoai,
        @Size(max = 100, message = "Học vị tối đa 100 ký tự") String hocVi,
        @Size(max = 150, message = "Chuyên môn tối đa 150 ký tự") String chuyenMon,
        @NotNull(message = "Vui lòng chọn Khoa trực thuộc") Long khoaId,
        Boolean active
) {
}
