package com.demo.be.dto.diem;

public record PendingApprovalClassDto(
        Long monHocMoId,
        String monHocTen,
        String monHocMa,
        Integer soTinChi,
        String tenLop,
        String maLop,
        String khoaHoc,
        String hocKy,
        String namHoc,
        String tenGiangVien,
        String maGiangVien,
        int soSinhVien,
        int gradedStudents,
        int totalStudents,
        String trangThaiDuyet,
        String ghiChuDuyet
) {
    public String getTenMonHoc() {
        return monHocTen;
    }

    public String getMaMonHoc() {
        return monHocMa;
    }
}

