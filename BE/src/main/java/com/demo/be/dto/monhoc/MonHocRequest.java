package com.demo.be.dto.monhoc;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MonHocRequest(
        @NotBlank(message = "Ma mon hoc is required") @Size(max = 20, message = "Ma mon hoc must be at most 20 characters") String maMonHoc,
        @NotBlank(message = "Ten mon hoc is required") @Size(max = 150, message = "Ten mon hoc must be at most 150 characters") String tenMonHoc,
        @NotNull(message = "So tin chi is required") @Min(value = 1, message = "So tin chi must be at least 1") Integer soTinChi,
        @NotNull(message = "So tiet ly thuyet is required") @Min(value = 0, message = "So tiet ly thuyet must not be negative") Integer soTietLyThuyet,
        @NotNull(message = "So tiet thuc hanh is required") @Min(value = 0, message = "So tiet thuc hanh must not be negative") Integer soTietThucHanh,
        @Size(max = 500, message = "Mo ta must be at most 500 characters") String moTa,
        @Size(max = 255, message = "Mon hoc tien quyet must be at most 255 characters") String monHocTienQuyet,
        @NotNull(message = "Khoa id is required") Long khoaId,
        Boolean active
) {
}
