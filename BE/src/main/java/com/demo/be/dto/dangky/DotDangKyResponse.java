package com.demo.be.dto.dangky;

import java.time.LocalDate;

public record DotDangKyResponse(
        Long id,
        String hocKy,
        String namHoc,
        String tenDot,
        boolean dangMo,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc
) {
}
