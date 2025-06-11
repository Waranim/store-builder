package xyz.waranim.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO для создания позиции заказа")
public record CreateOrderItemDto(
        @Schema(description = "UUID продукта", example = "f1a2b3c4-d5e6-7f89-0123-456789abcdef")
        UUID productId,

        @Schema(description = "Имя продукта на момент заказа", example = "T-Shirt")
        String productName,

        @Schema(description = "Цена за единицу", example = "19.99")
        BigDecimal unitPrice,

        @Schema(description = "Количество единиц", example = "2")
        Integer qty
) {
}
