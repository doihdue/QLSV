package com.demo.be.dto.renluyen;

import java.time.LocalDateTime;

public record BangDiemRenLuyenResponse(
        Long id,
        Long dotRenLuyenId,
        String tenDot,
        String hocKy,
        String namHoc,
        Long sinhVienId,
        String mssv,
        String hoTen,
        Long lopId,
        String tenLop,
        String tenKhoa,

        // Điểm SV
        Integer diemSvMuc1,
        Integer diemSvMuc2,
        Integer diemSvMuc3,
        Integer diemSvMuc4,
        Integer diemSvMuc5,
        Integer tongDiemSv,
        String ghiChuSv,
        LocalDateTime thoiGianSvNop,

        // Điểm GV
        Integer diemGvMuc1,
        Integer diemGvMuc2,
        Integer diemGvMuc3,
        Integer diemGvMuc4,
        Integer diemGvMuc5,
        Integer tongDiemGv,
        String nhanXetGv,
        String hoTenGiangVien,
        LocalDateTime thoiGianGvDanhGia,

        // Điểm Chốt & Xếp loại
        Integer diemTongKet,
        String xepLoai,
        String nhanXetAdmin,
        LocalDateTime thoiGianAdminDuyet,

        String trangThai,
        String trangThaiMoTa
) {
}
