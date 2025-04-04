package xyz.waranim.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с токеном доступа")
public record LoginResponse(
        @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
}
