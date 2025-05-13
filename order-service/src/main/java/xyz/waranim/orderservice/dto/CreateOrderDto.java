package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO для создания заказа")
public record CreateOrderDto(
        @Schema(description = "UUID магазина", example = "e7b7a1c0-3f4a-4bfc-8e2d-1234567890ab")
        UUID shopId,

        @Schema(description = "UUID клиента", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        UUID customerId,

        @Schema(description = "Список позиций заказа")
        List<CreateOrderItemDto> items
) {
}
