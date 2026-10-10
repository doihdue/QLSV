package com.demo.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu cũ là bắt buộc")
        String oldPassword,

        @NotBlank(message = "Mật khẩu mới là bắt buộc")
        @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
        String newPassword
) {
}
