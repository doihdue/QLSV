package com.demo.be.dto.renluyen;

import java.time.LocalDate;

public record DotRenLuyenResponse(
        Long id,
        String tenDot,
        String hocKy,
        String namHoc,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        boolean dangMo,
        String ghiChu,
        String timeStatus // "ACTIVE", "NOT_YET", "EXPIRED"
) {
}
