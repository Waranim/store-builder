package xyz.waranim.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на подтверждение OTP кода")
public record ConfirmRequest(
        @Schema(description = "Email пользователя", example = "user@example.com")
        @NotBlank(message = "Почта обязательна")
        String email,
        @Schema(description = "Одноразовый код подтверждения", example = "123456")
        @NotBlank(message = "OTP обязателен")
        String otp
) {
}
