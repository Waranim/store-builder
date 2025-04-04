package xyz.waranim.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на отправку OTP кода")
public record LoginRequest(
        @Schema(description = "Email пользователя", example = "user@example.com")
        @NotBlank(message = "Почта обязательна")
        @Email(message = "Невалидный формат почты")
        String email
) {
}
