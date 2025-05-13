package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import xyz.waranim.orderservice.entity.CustomerEntity;

import java.util.UUID;

@Schema(description = "DTO клиента")
public record CustomerDto(
        @Schema(description = "UUID клиента", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        UUID id,

        @Schema(description = "Email клиента", example = "ivan.ivanov@example.com")
        String email,

        @Schema(description = "Полное имя клиента", example = "Иван Иванов")
        String fullName
) {
    public static CustomerDto of(CustomerEntity entity) {
        return new CustomerDto(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName()
        );
    }
}
