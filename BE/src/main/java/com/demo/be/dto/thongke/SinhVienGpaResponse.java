package com.demo.be.dto.thongke;

public record SinhVienGpaResponse(
        Long sinhVienId,
        String mssv,
        String hoTen,
        String tenLop,
        String tenKhoa,
        Integer soMonHoc,
        Integer tinChiTichLuy,
        Double diemTrungBinh,
        String xepLoaiHocLuc
) {
    public static SinhVienGpaResponse fromProjection(SinhVienGpaProjection p) {
        return new SinhVienGpaResponse(
                p.getSinhVienId(),
                p.getMssv(),
                p.getHoTen(),
                p.getTenLop(),
                p.getTenKhoa(),
                p.getSoMonHoc() != null ? p.getSoMonHoc() : 0,
                p.getTinChiTichLuy() != null ? p.getTinChiTichLuy() : 0,
                p.getDiemTrungBinh() != null ? p.getDiemTrungBinh() : 0.0,
                p.getXepLoaiHocLuc() != null ? p.getXepLoaiHocLuc() : "Chưa có điểm"
        );
    }
}
