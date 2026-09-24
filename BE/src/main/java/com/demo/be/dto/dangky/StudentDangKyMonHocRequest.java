package com.demo.be.dto.dangky;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentDangKyMonHocRequest(
        @NotNull(message = "Mon hoc id is required") Long monHocId,
        @NotBlank(message = "Hoc ky is required") @Size(max = 20, message = "Hoc ky must be at most 20 characters") String hocKy,
        @NotBlank(message = "Nam hoc is required") @Size(max = 20, message = "Nam hoc must be at most 20 characters") String namHoc
) {
}
