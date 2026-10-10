package com.demo.be.dto.renluyen;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StudentDanhGiaDrlRequest(
        @NotNull(message = "Vui lòng chọn đợt đánh giá rèn luyện") Long dotId,

        @NotNull(message = "Điểm mục 1 không được để trống")
        @Min(value = 0, message = "Điểm mục 1 tối thiểu là 0")
        @Max(value = 20, message = "Điểm mục 1 (Ý thức học tập) tối đa là 20")
        Integer diemSvMuc1,

        @NotNull(message = "Điểm mục 2 không được để trống")
        @Min(value = 0, message = "Điểm mục 2 tối thiểu là 0")
        @Max(value = 25, message = "Điểm mục 2 (Ý thức chấp hành nội quy) tối đa là 25")
        Integer diemSvMuc2,

        @NotNull(message = "Điểm mục 3 không được để trống")
        @Min(value = 0, message = "Điểm mục 3 tối thiểu là 0")
        @Max(value = 20, message = "Điểm mục 3 (Hoạt động chính trị - xã hội) tối đa là 20")
        Integer diemSvMuc3,

        @NotNull(message = "Điểm mục 4 không được để trống")
        @Min(value = 0, message = "Điểm mục 4 tối thiểu là 0")
        @Max(value = 25, message = "Điểm mục 4 (Phẩm chất công dân) tối đa là 25")
        Integer diemSvMuc4,

        @NotNull(message = "Điểm mục 5 không được để trống")
        @Min(value = 0, message = "Điểm mục 5 tối thiểu là 0")
        @Max(value = 10, message = "Điểm mục 5 (Công tác tập thể) tối đa là 10")
        Integer diemSvMuc5,

        String ghiChuSv,

        String action // "SAVE_DRAFT" hoặc "SUBMIT"
) {
}
