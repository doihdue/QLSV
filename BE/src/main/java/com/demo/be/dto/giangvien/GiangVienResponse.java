package com.demo.be.dto.giangvien;

public record GiangVienResponse(
        Long id,
        String maGiangVien,
        String hoTen,
        String email,
        String soDienThoai,
        String hocVi,
        String chuyenMon,
        Long khoaId,
        String khoaTen,
        boolean active
) {
}
