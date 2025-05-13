package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO для создания клиента")
public record CreateCustomerDto(
        @Schema(description = "Email клиента", example = "ivan.ivanov@example.com")
        String email,

        @Schema(description = "Полное имя клиента", example = "Иван Иванов")
        String fullName
) {
}
