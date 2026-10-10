package com.demo.be.dto.giangvien;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GiangVienRequest(
        @Size(max = 20, message = "Mã giảng viên tối đa 20 ký tự") String maGiangVien,
        @NotBlank(message = "Họ và tên giảng viên là bắt buộc") @Size(max = 150, message = "Họ và tên tối đa 150 ký tự") String hoTen,
        @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Email không đúng định dạng (VD: giangvien@stu.edu.vn)")
        @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @Pattern(regexp = "^$|^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không đúng định dạng (gồm 10 chữ số, ví dụ: 0901234567 hoặc +84901234567)")
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String soDienThoai,
        @Size(max = 100, message = "Học vị tối đa 100 ký tự") String hocVi,
        @Size(max = 150, message = "Chuyên môn tối đa 150 ký tự") String chuyenMon,
        @NotNull(message = "Vui lòng chọn Khoa trực thuộc") Long khoaId,
        Boolean active
) {
}
