package com.demo.be.dto.renluyen;

public record ThongKeDrlResponse(
        long tongSinhVien,
        long chuaDanhGia,
        long choGiangVienDuyet,
        long choAdminDuyet,
        long daDuyet,
        long tuChoi,
        long countXuatSac,
        long countTot,
        long countKha,
        long countTrungBinh,
        long countYeu,
        long countKem,
        double diemTrungBinhToanLop
) {
}
