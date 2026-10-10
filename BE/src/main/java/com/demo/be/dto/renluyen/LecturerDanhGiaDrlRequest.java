package com.demo.be.dto.renluyen;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LecturerDanhGiaDrlRequest(
        @NotNull(message = "ID phiếu rèn luyện là bắt buộc") Long bangDiemId,

        @NotNull(message = "Điểm mục 1 không được để trống")
        @Min(value = 0, message = "Điểm mục 1 tối thiểu là 0")
        @Max(value = 20, message = "Điểm mục 1 tối đa là 20")
        Integer diemGvMuc1,

        @NotNull(message = "Điểm mục 2 không được để trống")
        @Min(value = 0, message = "Điểm mục 2 tối thiểu là 0")
        @Max(value = 25, message = "Điểm mục 2 tối đa là 25")
        Integer diemGvMuc2,

        @NotNull(message = "Điểm mục 3 không được để trống")
        @Min(value = 0, message = "Điểm mục 3 tối thiểu là 0")
        @Max(value = 20, message = "Điểm mục 3 tối đa là 20")
        Integer diemGvMuc3,

        @NotNull(message = "Điểm mục 4 không được để trống")
        @Min(value = 0, message = "Điểm mục 4 tối thiểu là 0")
        @Max(value = 25, message = "Điểm mục 4 tối đa là 25")
        Integer diemGvMuc4,

        @NotNull(message = "Điểm mục 5 không được để trống")
        @Min(value = 0, message = "Điểm mục 5 tối thiểu là 0")
        @Max(value = 10, message = "Điểm mục 5 tối đa là 10")
        Integer diemGvMuc5,

        String nhanXetGv,

        String action // "SAVE_DRAFT" hoặc "SUBMIT_ADMIN"
) {
}
