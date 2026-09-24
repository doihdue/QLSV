package com.demo.be.dto.monhoc;

public record MonHocResponse(
        Long id,
        String maMonHoc,
        String tenMonHoc,
        Integer soTinChi,
        Integer soTietLyThuyet,
        Integer soTietThucHanh,
        String moTa,
        String monHocTienQuyet,
        Long khoaId,
        String khoaTen,
        boolean active
) {
}
