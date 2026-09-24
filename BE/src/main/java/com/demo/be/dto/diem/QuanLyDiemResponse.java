package com.demo.be.dto.diem;

import java.math.BigDecimal;

public record QuanLyDiemResponse(
        Long id,
        Long dangKyMonHocId,
        Long sinhVienId,
        String sinhVienMssv,
        String sinhVienHoTen,
        Long monHocId,
        String monHocMa,
        String monHocTen,
        Integer soTinChi,
        String hocKy,
        String namHoc,
        BigDecimal diemChuyenCan,
        BigDecimal diemGiuaKy,
        BigDecimal diemCuoiKy,
        BigDecimal diemTongKet,
        String xepLoai,
        boolean dat,
        String trangThaiDuyet,
        String ghiChuDuyet
) {
}

