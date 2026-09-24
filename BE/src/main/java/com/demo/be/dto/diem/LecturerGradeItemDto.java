package com.demo.be.dto.diem;

import java.math.BigDecimal;

public record LecturerGradeItemDto(
        Long dangKyMonHocId,
        Long sinhVienId,
        String sinhVienMssv,
        String sinhVienHoTen,
        String tenLop,
        Long diemId,
        BigDecimal diemChuyenCan,
        BigDecimal diemGiuaKy,
        BigDecimal diemCuoiKy,
        BigDecimal diemTongKet,
        String xepLoai,
        boolean dat,
        String trangThaiDuyet,
        String ghiChuDuyet
) {
    public String getMssv() {
        return sinhVienMssv;
    }

    public String getHoTen() {
        return sinhVienHoTen;
    }

    public String getLopMa() {
        return tenLop;
    }

    public String getLopTen() {
        return tenLop;
    }
}

