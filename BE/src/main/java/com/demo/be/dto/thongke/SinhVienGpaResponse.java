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
    public SinhVienGpaResponse(
            Number sinhVienId,
            String mssv,
            String hoTen,
            String tenLop,
            String tenKhoa,
            Number soMonHoc,
            Number tinChiTichLuy,
            Number diemTrungBinh,
            String xepLoaiHocLuc
    ) {
        this(
                sinhVienId != null ? sinhVienId.longValue() : null,
                mssv != null ? mssv : "",
                hoTen != null ? hoTen : "",
                tenLop != null ? tenLop : "",
                tenKhoa != null ? tenKhoa : "",
                soMonHoc != null ? soMonHoc.intValue() : 0,
                tinChiTichLuy != null ? tinChiTichLuy.intValue() : 0,
                diemTrungBinh != null ? diemTrungBinh.doubleValue() : 0.0,
                xepLoaiHocLuc != null ? xepLoaiHocLuc : "Chưa có điểm"
        );
    }

    public static SinhVienGpaResponse fromProjection(SinhVienGpaProjection p) {
        if (p == null) return null;
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
