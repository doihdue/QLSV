package com.demo.be.dto.monhocmo;

public record MonHocMoResponse(
        Long id,
        Long monHocId,
        String monHocMa,
        String tenMonHoc,
        Integer soTinChi,
        Integer soTietLyThuyet,
        Integer soTietThucHanh,
        String monHocTienQuyet,
        Long khoaId,
        String tenKhoa,
        String khoaHoc,
        String hocKy,
        String namHoc,
        Long lopId,
        String tenLop,
        String maLop,
        Long giangVienId,
        String maGiangVien,
        String tenGiangVien,
        String ghiChu
) {
}
