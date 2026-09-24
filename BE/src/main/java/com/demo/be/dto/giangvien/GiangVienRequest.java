package com.demo.be.dto.giangvien;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GiangVienRequest(
        @Size(max = 20, message = "Ma giang vien must be at most 20 characters") String maGiangVien,
        @NotBlank(message = "Ho ten is required") @Size(max = 150, message = "Ho ten must be at most 150 characters") String hoTen,
        @Size(max = 150, message = "Email must be at most 150 characters") String email,
        @Size(max = 20, message = "So dien thoai must be at most 20 characters") String soDienThoai,
        @Size(max = 100, message = "Hoc vi must be at most 100 characters") String hocVi,
        @Size(max = 150, message = "Chuyen mon must be at most 150 characters") String chuyenMon,
        @NotNull(message = "Khoa id is required") Long khoaId,
        Boolean active
) {
}
