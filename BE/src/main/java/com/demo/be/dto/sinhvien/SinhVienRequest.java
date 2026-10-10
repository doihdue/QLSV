package com.demo.be.dto.sinhvien;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SinhVienRequest(
        @Size(max = 20, message = "MSSV tối đa 20 ký tự") String mssv,
        @NotBlank(message = "Họ và tên sinh viên là bắt buộc") @Size(max = 150, message = "Họ và tên tối đa 150 ký tự") String hoTen,
        @Past(message = "Ngày sinh phải là ngày trong quá khứ") LocalDate ngaySinh,
        @Size(max = 10, message = "Giới tính tối đa 10 ký tự") String gioiTinh,
        @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Email không đúng định dạng (VD: example@stu.edu.vn)")
        @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @Pattern(regexp = "^$|^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không đúng định dạng (gồm 10 chữ số, ví dụ: 0912345678 hoặc +84912345678)")
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String soDienThoai,
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự") String diaChi,
        LocalDate ngayNhapHoc,
        @NotNull(message = "Vui lòng chọn Lớp hành chính") Long lopId,
        Boolean active
) {
}
