package com.demo.be.dto.dangky;

import java.time.LocalDateTime;

public record DangKyMonHocResponse(
        Long id,
        Long sinhVienId,
        String sinhVienMssv,
        String sinhVienHoTen,
        Long lopId,
        String lopMa,
        String lopTen,
        Long monHocId,
        String monHocMa,
        String monHocTen,
        Integer soTinChi,
        String hocKy,
        String namHoc,
        LocalDateTime ngayDangKy,
        String trangThai
) {
}

